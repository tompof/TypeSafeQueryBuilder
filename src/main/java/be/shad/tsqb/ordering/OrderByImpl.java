/*
 * Copyright Gert Wijns gert.wijns@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.shad.tsqb.ordering;

import be.shad.tsqb.hql.HqlQuery;
import be.shad.tsqb.query.copy.CopyContext;
import be.shad.tsqb.query.copy.Copyable;
import be.shad.tsqb.values.HqlQueryBuilderParams;
import be.shad.tsqb.values.HqlQueryValue;
import be.shad.tsqb.values.TypeSafeValue;

public class OrderByImpl implements OrderBy {
    private TypeSafeValue<?> value;
    private boolean descending;

    public OrderByImpl(TypeSafeValue<?> value, boolean descending) {
        this.value = value;
        this.descending = descending;
    }

    /**
     * Copy constructor
     */
    protected OrderByImpl(CopyContext context, OrderByImpl original) {
        this.value = context.get(original.value);
        this.descending = original.descending;
    }

    @Override
    public void appendTo(HqlQuery query, HqlQueryBuilderParams params) {
        //ascending is the default
        String order = descending ? " desc": "";
        HqlQueryValue hqlValue = value.toHqlQueryValue(params);
		query.appendOrderBy(hqlValue.getHql() + order);
		query.addParams(hqlValue.getParams());
    }

    @Override
    public Copyable copy(CopyContext context) {
        return new OrderByImpl(context, this);
    }

}
