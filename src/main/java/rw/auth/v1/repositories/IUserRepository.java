package rw.auth.v1.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rw.auth.v1.enums.EGender;
import rw.auth.v1.enums.EUserStatus;
import rw.auth.v1.models.Role;
import rw.auth.v1.models.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNationalId(String nationalId);

    Integer countByStatus(EUserStatus status);

    Integer countByStatusAndRolesContaining(EUserStatus status, Role role);

    boolean existsByActivationCodeAndEmail(String activationCode, String email);

    Optional<User> findByEmailOrPhoneNumberOrNationalId(String email, String phoneNumber, String nationalId);

    Optional<User> findByStatusNotAndEmailOrPhoneNumberOrNationalId(EUserStatus status, String email, String phoneNumber, String nationalId);

    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);

    List<User> findByRolesContaining(Role role);

    List<User> findByRolesContainingAndStatus(Role role, EUserStatus status);

    Page<User> findByRolesContaining(Role roles, Pageable pageable);

    Page<User> findByStatus(EUserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName)  LIKE CONCAT('%', LOWER(:searchKey) , '%')  OR LOWER(u.lastName) LIKE CONCAT('%', LOWER(:searchKey) , '%')  OR LOWER(u.email) LIKE CONCAT('%', lower(:searchKey),'%')")
    List<User> searchUser(String searchKey);


    @Query("SELECT u FROM User u WHERE  ( (u.email = :email ) OR (u.phoneNumber = :phoneNumber ) OR (u.nationalId = :nationalId) ) AND (u.status <> :status) ")
    Optional<User> findByEmailOrPhoneNumberOrNationalIdAndStatusNot(String email, String phoneNumber, String nationalId, EUserStatus status);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName)  LIKE CONCAT('%', LOWER(:searchKey) , '%')  OR LOWER(u.lastName) LIKE CONCAT('%', LOWER(:searchKey) , '%')  OR LOWER(u.email) LIKE CONCAT('%', lower(:searchKey),'%')")
    Page<User> searchUser(Pageable pageable, String searchKey);

    @Query("SELECT u FROM User u WHERE u.status = :status AND u.gender = :gender AND LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE CONCAT('%', LOWER(:fullNames), '%')")
    Page<User> findByStatusAndGenderAndSearchByFullName(EUserStatus status, EGender gender, String fullNames, Pageable pageable);

    @Query("SELECT u FROM User u WHERE (:role MEMBER OF u.roles)  AND u.status = :status AND u.gender = :gender AND LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE CONCAT('%', LOWER(:fullNames), '%')")
    Page<User> findByRoleAndStatusAndGenderAndSearchByFullName(Role role, EUserStatus status, EGender gender, String fullNames, Pageable pageable);

    @Query("SELECT u FROM User u  WHERE u.status = :status AND LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE CONCAT('%', LOWER(:fullNames), '%')")
    Page<User> findByStatusAndFirstNameContains(EUserStatus status, String fullNames, Pageable pageable);

    @Query("SELECT u FROM User u WHERE (:role MEMBER OF u.roles) AND u.status = :status AND LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE CONCAT('%', LOWER(:fullNames), '%')")
    Page<User> findByRoleAndStatusAndFirstNameContains(Role role, EUserStatus status, String fullNames, Pageable pageable);

}
