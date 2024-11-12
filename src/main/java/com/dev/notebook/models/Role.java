package com.dev.notebook.models;

import com.dev.notebook.enumeration.Authority;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Entity
@Table(name = "tbl_roles")
public class Role extends BaseAuditable {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String authorities;

    public Role(String name, Authority authorities) {
        super();
    }


    public void setAuthority(Authority authority) {
        if (authority != null) {
            this.authorities = authority.toString();
        }
    }


    public void setAuthorities(Authority[] authorities) {
        if (authorities != null && authorities.length > 0) {
            this.authorities = Arrays.stream(authorities)
                    .map(Authority::toString)
                    .collect(Collectors.joining(","));
        }
    }

    public Authority[] getAuthoritiesArray() {
        return this.authorities != null ?
                Arrays.stream(this.authorities.split(","))
                        .map(Authority::valueOf)
                        .toArray(Authority[]::new)
                : new Authority[0];
    }

    public String getAuthoritiesValues() {
        return this.authorities != null ?
                String.join(",", this.authorities.split(","))
                : "";
    }

}
