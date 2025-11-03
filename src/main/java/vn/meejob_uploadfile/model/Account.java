package vn.meejob_uploadfile.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.meejob_uploadfile.utils.Role;

@Getter
@AllArgsConstructor
public class Account {
    private Long id;
    private String username;
    private Role role;
}
