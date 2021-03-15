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
package org.apache.syncope.core.rest.cxf.service.wa;

import java.net.URI;
import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.syncope.common.lib.to.PagedResult;
import org.apache.syncope.common.lib.wa.U2FDevice;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.apache.syncope.common.rest.api.beans.U2FDeviceQuery;
import org.apache.syncope.common.rest.api.service.wa.U2FRegistrationService;
import org.apache.syncope.core.logic.wa.U2FRegistrationLogic;
import org.apache.syncope.core.rest.cxf.service.AbstractServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class U2FRegistrationServiceImpl extends AbstractServiceImpl implements U2FRegistrationService {

    @Autowired
    private U2FRegistrationLogic logic;

    @Override
    public Response delete(final U2FDeviceQuery query) {
        logic.delete(query.getEntityKey(), query.getId(), query.getExpirationDate());
        return Response.noContent().build();
    }

    @Override
    public void update(final U2FDevice device) {
        logic.update(device);
    }

    @Override
    public Response create(final String owner, final U2FDevice device) {
        String key = logic.create(owner, device);
        URI location = uriInfo.getAbsolutePathBuilder().path(key).build();
        return Response.created(location).
                header(RESTHeaders.RESOURCE_KEY, key).
                build();
    }

    @Override
    public PagedResult<U2FDevice> search(final U2FDeviceQuery query) {
        Pair<Integer, List<U2FDevice>> result = logic.search(
                query.getEntityKey(),
                query.getPage(),
                query.getSize(),
                query.getId(),
                query.getExpirationDate(),
                getOrderByClauses(query.getOrderBy()));
        return buildPagedResult(result.getRight(), query.getPage(), query.getSize(), result.getLeft());
    }

    @Override
    public U2FDevice read(final String key) {
        return logic.read(key);
    }
}
