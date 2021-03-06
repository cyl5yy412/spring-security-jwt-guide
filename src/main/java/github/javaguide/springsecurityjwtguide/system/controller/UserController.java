package github.javaguide.springsecurityjwtguide.system.controller;

import github.javaguide.springsecurityjwtguide.security.entity.CurrentUser;
import github.javaguide.springsecurityjwtguide.system.entity.User;
import github.javaguide.springsecurityjwtguide.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Chr.yl
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    private final CurrentUser currentUser;

    public UserController(UserService userService, CurrentUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ROLE_DEV','ROLE_PM','ROLE_ADMIN')")
    public ResponseEntity<Page<User>> getAllUser(@RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                                                 @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        System.out.println("当前访问该接口的用户为：" + currentUser.getCurrentUser().toString());
        Page<User> allUser = userService.getAllUser(pageNum, pageSize);
        return ResponseEntity.ok().body(allUser);
    }


    @DeleteMapping("/user")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<User> deleteUserById(@RequestParam("username") String username) {
        userService.deleteUserByUserName(username);
        return ResponseEntity.ok().build();
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/z")
    @PreAuthorize("hasAnyRole('ROLE_DEV','ROLE_PM')")
    public Object z(@RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Map<String, Object> map = jdbcTemplate.queryForMap("select * from user");
        return map;

    }
}
