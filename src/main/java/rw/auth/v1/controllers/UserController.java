package rw.auth.v1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rw.auth.v1.dtos.*;
import rw.auth.v1.enums.EGender;
import rw.auth.v1.enums.ERole;
import rw.auth.v1.enums.EUserStatus;
import rw.auth.v1.fileHandling.File;
import rw.auth.v1.fileHandling.FileStorageService;
import rw.auth.v1.models.Role;
import rw.auth.v1.models.User;
import rw.auth.v1.payload.ApiResponse;
import rw.auth.v1.services.IFileService;
import rw.auth.v1.services.INotificationService;
import rw.auth.v1.services.IRoleService;
import rw.auth.v1.services.IUserService;
import rw.auth.v1.utils.Constants;
import rw.auth.v1.utils.Formatter;
import rw.auth.v1.utils.Mapper;

import javax.validation.Valid;
import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController {

    private final IUserService userService;
    private final IRoleService roleService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final FileStorageService fileStorageService;
    private final IFileService fileService;
    private final INotificationService notificationService;

    @Value("${uploads.directory.user_profiles}")
    private String directory;

    @Autowired
    public UserController(IUserService userService, IRoleService iRoleService, BCryptPasswordEncoder bCryptPasswordEncoder, FileStorageService fileStorageService, IFileService fileService, INotificationService notificationService) {
        this.userService = userService;
        this.roleService = iRoleService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.fileService = fileService;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    @GetMapping(path = "/current-user")
    public ResponseEntity<ApiResponse> currentlyLoggedInUser() {
        return ResponseEntity.ok(new ApiResponse(true, userService.getLoggedInUser()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers(@RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page, @RequestParam(value = "size", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.Direction.ASC, "id");

        return ResponseEntity.ok(ApiResponse.success(this.userService.getAll(pageable)));
    }

    @GetMapping(path = "/all")
    public ResponseEntity<ApiResponse> getAllPaginatedUsers(@RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page, @RequestParam(value = "size", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.Direction.ASC, "id");

        return ResponseEntity.ok(ApiResponse.success(userService.getAll(pageable)));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<User> getById(@PathVariable(value = "id") UUID id) {
        return ResponseEntity.ok(this.userService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> search(
            @RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "limit", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit,
            @RequestParam(value = "name", required = false, defaultValue = "") String name,
            @RequestParam(value = "status") EUserStatus status,
            @RequestParam(value = "gender", required = false) EGender gender,
            @RequestParam(value = "role", required = false) ERole role

    ){
        Pageable pageable = PageRequest.of(page, limit, Sort.Direction.ASC, "id");

        if(role != null) {
            Role roleSearch = roleService.findByName(role);
            return ResponseEntity.ok(ApiResponse.success(userService.search(roleSearch, status, name, gender, pageable)));
        }else{
            return ResponseEntity.ok(ApiResponse.success(userService.search(status, name, gender, pageable)));
        }

    }

    @PostMapping(path = "/register")
    public ResponseEntity<ApiResponse> register(@RequestBody @Valid SignUpDTO dto) {

        User user = new User();

        String encodedPassword = bCryptPasswordEncoder.encode(dto.getPassword());
        Role role = roleService.findByName(dto.getRole());

        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setGender(dto.getGender());
        user.setPhoneNumber(dto.getMobile());
        user.setPassword(encodedPassword);
        user.setStatus(EUserStatus.ACTIVE);
        user.setRoles(Collections.singleton(role));

        User entity = this.userService.create(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, entity));
    }

    @PostMapping(path = "/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestBody @Valid VerifyUserEmailDTO dto) {
        User user = userService.findById(dto.getUserId());
        userService.verifyEmail(user);
        return ResponseEntity.ok(new ApiResponse(true, "Email was successfully verified"));
    }

    @PutMapping(path = "/{id}/upload-profile")
    public ResponseEntity<ApiResponse> uploadProfileImage(
            @PathVariable(value = "id") UUID id,
            @RequestParam("file") MultipartFile document
    ) {
        this.userService.findById(id);
        File file = this.fileService.create(document, directory);

        User updated = this.userService.changeProfileImage(id, file);

        return ResponseEntity.ok(new ApiResponse(true, "File saved successfully", updated));

    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse> approveAUser(@PathVariable UUID id) {
        User user = userService.findById(id);

        userService.approve(user);

        return ResponseEntity.ok(ApiResponse.success("Approved User Successfully"));
    }

    @PutMapping("/{id}/de-activate")
    public ResponseEntity<ApiResponse> deActivateAnAccount(@PathVariable UUID id) {
        User user = userService.findById(id);

        userService.deActivate(user);

        return ResponseEntity.ok(ApiResponse.success("De-Activate User Successfully"));
    }

    @PutMapping("/{id}/mark-as/PENDING")
    public ResponseEntity<ApiResponse> markUserAsPending(@PathVariable UUID id) {
        User user = userService.findById(id);

        user.setStatus(EUserStatus.PENDING);

        userService.save(user);

        return ResponseEntity.ok(ApiResponse.success("Marked User as Pending Successfully"));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse> rejectAUser(@PathVariable UUID id, @Valid @RequestBody RejectionDTO dto) {
        User user = userService.findById(id);

        userService.reject(user, dto.getRejectionMessage());

        return ResponseEntity.ok(ApiResponse.success("User Rejected Successfully"));
    }

    @PutMapping("/reject-many")
    public ResponseEntity<ApiResponse> rejectManyUsers(@Valid @RequestBody RejectManyUsersDTO dto) {

        userService.rejectManyUsers(dto.getUsersIds(), dto.getRejectionMessage());

        return ResponseEntity.ok(ApiResponse.success("Rejected all users"));
    }

    @PutMapping("/approve-many")
    public ResponseEntity<ApiResponse> approveManyUsers(@Valid @RequestBody ApproveManyUsersDTO dto) {

        userService.approveManyUsers(dto.getUserIds());

        return ResponseEntity.ok(ApiResponse.success("Approved all the Users"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@Valid @RequestBody UpdateUserDTO dto, @PathVariable UUID id){
        User theUser = Mapper.getUserFromDTO(dto);

        theUser = userService.update(id, theUser);
        return ResponseEntity.accepted().body(ApiResponse.success(theUser));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(ChangePasswordDTO dto){
        userService.changePassword(dto);

        return Formatter.done();
    }

    @GetMapping("/load-file/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> loadProfileImage(@PathVariable String filename) {

        Resource file = this.fileStorageService.load(directory, filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse> delete(@PathVariable UUID id){
        notificationService.deleteByUserId(id);
        return ResponseEntity.ok(ApiResponse.success(userService.delete(id)));
    }
}