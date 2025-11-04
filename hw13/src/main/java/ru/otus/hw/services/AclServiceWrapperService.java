package ru.otus.hw.services;

import org.springframework.security.acls.model.Permission;

public interface AclServiceWrapperService {

    void createPermission(Object object, Permission permission);

    void createAdminPermission(Object object);

}
