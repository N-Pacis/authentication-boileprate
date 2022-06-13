package rw.auth.v1.serviceImpls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.auth.v1.dtos.ChangePasswordDTO;
import rw.auth.v1.enums.EGender;
import rw.auth.v1.enums.ENotificationType;
import rw.auth.v1.enums.ERole;
import rw.auth.v1.enums.EUserStatus;
import rw.auth.v1.exceptions.BadRequestException;
import rw.auth.v1.exceptions.ResourceNotFoundException;
import rw.auth.v1.fileHandling.File;
import rw.auth.v1.models.Role;
import rw.auth.v1.models.User;
import rw.auth.v1.repositories.IUserRepository;
import rw.auth.v1.services.INotificationService;
import rw.auth.v1.services.IRoleService;
import rw.auth.v1.services.IUserService;
import rw.auth.v1.services.MailService;
import rw.auth.v1.utils.Mapper;
import rw.auth.v1.utils.Profile;

import java.util.*;

@Service
public class UserServiceImpl implements IUserService {

    private final MailService mailService;
    private final IRoleService roleService;
    private final IUserRepository userRepository;
    private final INotificationService notificationService;

    @Autowired
    public UserServiceImpl(IRoleService iRoleService, MailService mailService, IUserRepository userRepository, @Lazy INotificationService notificationService) {
        this.mailService = mailService;
        this.roleService = iRoleService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public List<User> getAll() {
        return this.userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    public Page<User> getAll(Pageable pageable) {
        return this.userRepository.findAll(pageable);
    }

    @Override
    public Page<User> getAllActive(Pageable pageable) {
        return userRepository.findByStatus(EUserStatus.ACTIVE, pageable);
    }

    @Override
    public Page<User> getAllRejected(Pageable pageable) {
        return userRepository.findByStatus(EUserStatus.REJECTED, pageable);
    }

    @Override
    public User findById(UUID id) {
        return this.userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", id.toString()));
    }

    @Override
    public User create(User user) {
        validateNewRegistration(user);

        mailService.sendAccountVerificationEmail(user);

        return this.userRepository.save(user);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User update(UUID id, User user) {
        User entity = this.userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", id.toString()));

        Optional<User> userOptional = this.userRepository.findByEmailOrPhoneNumberOrNationalId(user.getEmail(), user.getPhoneNumber(), user.getNationalId());
        if (userOptional.isPresent() && (userOptional.get().getId() != entity.getId()))
            throw new BadRequestException(String.format("User with email '%s' or phone number '%s' or national id '%s' already exists", user.getEmail(), user.getPhoneNumber(), user.getNationalId()));

        entity.setEmail(user.getEmail());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setPhoneNumber(user.getPhoneNumber());
        entity.setGender(user.getGender());
        entity.setNationalId(user.getNationalId());

        return this.userRepository.save(entity);
    }

    @Override
    public boolean delete(UUID id) {
        this.userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("User", "id", id));

        this.userRepository.deleteById(id);
        return true;
    }

    @Override
    public boolean isNotUnique(User user) {
        Optional<User> userOptional = this.userRepository.findByEmailOrPhoneNumberOrNationalId(user.getEmail(), user.getPhoneNumber(), user.getNationalId());
        return userOptional.isPresent();
    }

    @Override
    public boolean isNotUniqueInVerified(User user) {
        try {
            Optional<User> userOptional = this.userRepository.findByEmailOrPhoneNumberOrNationalIdAndStatusNot(user.getEmail(), user.getPhoneNumber(), user.getNationalId(), EUserStatus.WAIT_EMAIL_VERIFICATION);
            return userOptional.isPresent();
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public void validateNewRegistration(User user) {
        if (isNotUniqueInVerified(user)) {
            throw new BadRequestException(String.format("User with email '%s' or phone number '%s' or national id '%s' already exists", user.getEmail(), user.getPhoneNumber(), user.getNationalId()));
        } else if (isNotUnique(user)) {
            Optional<User> userToUpdate2 = userRepository.findByNationalId(user.getNationalId());

            if (userToUpdate2.isPresent()) {
                user.setId(userToUpdate2.get().getId());
            } else {
                throw new BadRequestException(String.format("User with email '%s' or phone number '%s'", user.getEmail(), user.getPhoneNumber()));
            }
        }
    }

    @Override
    public List<User> getAllByRole(ERole role) {
        Role theRole = roleService.findByName(role);

        return this.userRepository.findByRolesContaining(theRole);
    }

    @Override
    public List<User> getAllByRoleAndActive(ERole roleName) {
        Role role = roleService.findByName(roleName);

        return this.userRepository.findByRolesContainingAndStatus(role, EUserStatus.ACTIVE);
    }

    @Override
    public Page<User> getAllByRole(Pageable pageable, ERole role) {
        Role theRole = roleService.findByName(role);

        return this.userRepository.findByRolesContaining(theRole, pageable);
    }

    @Override
    public List<User> searchUser(String searchKey) {
        return this.userRepository.searchUser(searchKey);
    }


    @Override
    public Page<User> searchUser(Pageable pageable, String searchKey) {
        return this.userRepository.searchUser(pageable, searchKey);
    }

    @Override
    public User getLoggedInUser() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() == "anonymousUser")
            throw new BadRequestException("You are not logged in, try to log in");

        String email;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        return userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", email));
    }

    @Override
    public Profile getLoggedInProfile() {
        User theUser = getLoggedInUser();
        Object profile;
        Optional<Role> role = theUser.getRoles().stream().findFirst();
        if (role.isPresent()) {
            switch (role.get().getName()) {
                case ADMIN:
                    profile = theUser;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + role.get().getName());
            }

            return new Profile(profile);
        }

        return null;
    }

    @Override
    public User getByEmail(String email) {
        return this.userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User", "email", email));
    }

    @Override
    public User approve(User user) {
        if (user.getStatus() == EUserStatus.ACTIVE)
            throw new BadRequestException("User Already Approved  ");

        if (user.getStatus() == EUserStatus.REJECTED)
            throw new BadRequestException("User was rejected previously");

        user.setStatus(EUserStatus.ACTIVE);

        mailService.sendWelcomeEmailMail(user);

        return userRepository.save(user);
    }

    @Override
    public void approveManyUsers(List<UUID> userIds) {
        List<User> users = new ArrayList<>();
        for (UUID id : userIds)
            users.add(findById(id));

        for (User user : users)
            approve(user);
    }

    @Override
    public User reject(User user, String rejectionMessage) {
        if (user.getStatus() == EUserStatus.REJECTED)
            throw new BadRequestException("User Already Rejected ");


        if (user.getStatus() == EUserStatus.ACTIVE)
            throw new BadRequestException("User was approved recently");


        user.setStatus(EUserStatus.REJECTED);
        user.setRejectionDescription(rejectionMessage);

        mailService.sendAccountRejectedMail(user);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void rejectManyUsers(List<UUID> userIds, String message) {
        for (UUID id : userIds) {
            reject(findById(id), message);
        }
    }

    @Override
    public User changeStatus(UUID id, EUserStatus status) {
        User entity = this.userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", id.toString()));

        entity.setStatus(status);

        return this.userRepository.save(entity);
    }

    @Override
    public void verifyEmail(String email, String activationCode) {
        User user = getByEmail(email);

        if (!Objects.equals(user.getActivationCode(), activationCode))
            throw new BadRequestException("Invalid Activation Code ..");

        verifyEmail(user);
    }

    @Override
    public void verifyEmail(User user) {

        if (user.getStatus() != EUserStatus.WAIT_EMAIL_VERIFICATION)
            throw new BadRequestException("Your account is " + user.getStatus().toString().toLowerCase(Locale.ROOT));

        user.setStatus(EUserStatus.PENDING);

        userRepository.save(user);

        mailService.sendEmailVerifiedMail(user);

        List<User> usersToNotify = new ArrayList<>();

        ERole role = user.getRole();
        ENotificationType type;
        String notificationMessage;

        switch (role) {
            case STANDARD:
                usersToNotify.addAll(getAllByRoleAndActive(ERole.ADMIN));
                type = ENotificationType.USER_AWAITS_CONFIRMATION;
                notificationMessage = String.format(" You have a new User Registration which awaits your approval.");
                break;
            default:
                throw new BadRequestException("You have an invalid role for email verification");

        }

        notificationService.notifyBulk(usersToNotify, type, notificationMessage);

    }

    @Override
    public void deActivate(User user) {
        user.setStatus(EUserStatus.DEACTIVATED);
        userRepository.save(user);
    }

    @Override
    public User changeProfileImage(UUID id, File file) {
        User entity = this.userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Document", "id", id.toString()));

        entity.setProfileImage(file);
        return this.userRepository.save(entity);

    }

    @Override
    public Page<User> search(EUserStatus status, String name, EGender gender, Pageable pageable) {
        if (gender == null)
            return userRepository.findByStatusAndFirstNameContains(status, name, pageable);
        else
            return userRepository.findByStatusAndGenderAndSearchByFullName(status, gender, name, pageable);
    }

    @Override
    public Page<User> search(Role role, EUserStatus status, String name, EGender gender, Pageable pageable) {
        if (role != null) {
            if (gender == null) {
                return userRepository.findByRoleAndStatusAndFirstNameContains(role, status, name, pageable);
            } else {
                return userRepository.findByRoleAndStatusAndGenderAndSearchByFullName(role, status, gender, name, pageable);
            }
        } else {
            return search(status, name, gender, pageable);
        }
    }

    @Override
    public boolean isCodeValid(String email, String activationCode) {
        return userRepository.existsByActivationCodeAndEmail(activationCode, email);
    }

    @Override
    public Integer countAllActive() {
        return userRepository.countByStatus(EUserStatus.ACTIVE);
    }

    @Override
    public Integer countAllActiveByRole(Role role) {
        return userRepository.countByStatusAndRolesContaining(EUserStatus.ACTIVE, role);
    }

    @Override
    public void changePassword(ChangePasswordDTO dto) {
        User user = getLoggedInUser();

        if (Mapper.compare(user.getPassword(), dto.getCurrentPassword()))
            throw new BadRequestException("Invalid current password");

        user.setPassword(Mapper.encode(dto.getNewPassword()));

        userRepository.save(user);
    }
}
