package spring.ecommerce.service;

import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.dao.RoleDao;
import spring.ecommerce.entity.RoleEntity;
import spring.ecommerce.exception.RoleNotFoundException;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class RoleService {

    private final RoleDao roleDao;

    /**
     * Creates a new role in the system.
     *
     * @param role the role to be saved
     * @return the saved role
     */
    public RoleEntity createNewRole(RoleEntity role) {
        log.info("Creating a new role: {}", role.getRoleName());
        RoleEntity savedRole = roleDao.save(role);
        log.info("Role '{}' created successfully.", savedRole.getRoleName());
        return savedRole;
    }

    /**
     * Finds a role by name.
     *
     * @param roleName the name of the role to search for
     * @return the found role
     * @throws RoleNotFoundException if the role does not exist
     */
    public RoleEntity findByRoleName(String roleName) {
        log.info("Searching for role: {}", roleName);
        return Optional.ofNullable(roleDao.findByRoleName(roleName))
                .orElseThrow(() -> {
                    log.warn("Role '{}' not found", roleName);
                    return new RoleNotFoundException("Role '" + roleName + "' not found");
        });
    }
}
