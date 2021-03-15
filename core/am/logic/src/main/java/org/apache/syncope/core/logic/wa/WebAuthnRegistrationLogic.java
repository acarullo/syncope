/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.core.logic.wa;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.syncope.common.lib.types.AMEntitlement;
import org.apache.syncope.common.lib.types.IdRepoEntitlement;
import org.apache.syncope.common.lib.wa.WebAuthnAccount;
import org.apache.syncope.common.lib.wa.WebAuthnDeviceCredential;
import org.apache.syncope.core.logic.AbstractAuthProfileLogic;
import org.apache.syncope.core.persistence.api.dao.NotFoundException;
import org.apache.syncope.core.persistence.api.entity.EntityFactory;
import org.apache.syncope.core.persistence.api.entity.auth.AuthProfile;
import org.apache.syncope.core.spring.security.SecureRandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class WebAuthnRegistrationLogic extends AbstractAuthProfileLogic {

    @Autowired
    private EntityFactory entityFactory;

    @PreAuthorize("hasRole('" + AMEntitlement.WEBAUTHN_READ_DEVICE + "') "
            + "or hasRole('" + IdRepoEntitlement.ANONYMOUS + "')")
    @Transactional(readOnly = true)
    public WebAuthnAccount read(final String key) {
        return authProfileDAO.findAll().
                stream().
                map(AuthProfile::getWebAuthnAccount).
                filter(record -> record.getKey().equals(key)).
                findFirst().
                orElseThrow(() -> new NotFoundException("Could not find account with key " + key));
    }

    @PreAuthorize("hasRole('" + AMEntitlement.WEBAUTHN_LIST_DEVICE + "') "
            + "or hasRole('" + IdRepoEntitlement.ANONYMOUS + "')")
    @Transactional(readOnly = true)
    public List<WebAuthnAccount> list() {
        return authProfileDAO.findAll().stream().
                map(AuthProfile::getWebAuthnAccount).
                collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('" + AMEntitlement.WEBAUTHN_READ_DEVICE + "') "
            + "or hasRole('" + IdRepoEntitlement.ANONYMOUS + "')")
    @Transactional(readOnly = true)
    public WebAuthnAccount findAccountBy(final String owner) {
        return authProfileDAO.findByOwner(owner).
                stream().
                map(AuthProfile::getWebAuthnAccount).
                findFirst().
                orElseThrow(() -> new NotFoundException("Could not find account for Owner " + owner));
    }

    @PreAuthorize("hasRole('" + AMEntitlement.WEBAUTHN_DELETE_DEVICE + "') "
            + "or hasRole('" + IdRepoEntitlement.ANONYMOUS + "')")
    public void delete(final String owner) {
        authProfileDAO.findByOwner(owner).ifPresent(profile -> {
            profile.setWebAuthnAccount(null);
            authProfileDAO.save(profile);
        });
    }

    @PreAuthorize("hasRole('" + AMEntitlement.WEBAUTHN_DELETE_DEVICE + "') "
            + "or hasRole('" + IdRepoEntitlement.ANONYMOUS + "')")
    public void delete(final String owner, final String credentialId) {
        authProfileDAO.findByOwner(owner).
                stream().
                findFirst().
                ifPresent(profile -> {
                    WebAuthnAccount webAuthnAccount = profile.getWebAuthnAccount();
                    List<WebAuthnDeviceCredential> accounts = webAuthnAccount.getCredentials();
                    if (accounts.removeIf(acct -> acct.getIdentifier().equals(credentialId))) {
                        profile.setWebAuthnAccount(webAuthnAccount);
                        authProfileDAO.save(profile);
                    }
                });
    }

    @PreAuthorize("hasRole('" + AMEntitlement.WEBAUTHN_CREATE_DEVICE + "') "
            + "or hasRole('" + IdRepoEntitlement.ANONYMOUS + "')")
    public WebAuthnAccount create(final String owner, final WebAuthnAccount account) {
        if (account.getKey() == null) {
            account.setKey(SecureRandomUtils.generateRandomUUID().toString());
        }

        AuthProfile profile = authProfileDAO.findByOwner(owner).orElseGet(() -> {
            AuthProfile authProfile = entityFactory.newEntity(AuthProfile.class);
            authProfile.setOwner(owner);
            return authProfile;
        });
        profile.setWebAuthnAccount(account);
        authProfileDAO.save(profile);
        return profile.getWebAuthnAccount();
    }

    @PreAuthorize("hasRole('" + AMEntitlement.WEBAUTHN_UPDATE_DEVICE + "') "
            + "or hasRole('" + IdRepoEntitlement.ANONYMOUS + "')")
    public void update(final String owner, final WebAuthnAccount account) {
        authProfileDAO.findByOwner(owner).ifPresent(profile -> {
            profile.setWebAuthnAccount(account);
            authProfileDAO.save(profile);
        });
    }
}
