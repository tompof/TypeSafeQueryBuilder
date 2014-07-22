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
package be.shad.tsqb.test.restrictions;

import static be.shad.tsqb.restrictions.RestrictionsGroupImpl.group;
import static be.shad.tsqb.restrictions.predicate.RestrictionValuePredicate.IGNORE_EMPTY_COLLECTION;
import static be.shad.tsqb.restrictions.predicate.RestrictionValuePredicate.IGNORE_EMPTY_STRING;
import static be.shad.tsqb.restrictions.predicate.RestrictionValuePredicate.IGNORE_NULL;
import static be.shad.tsqb.restrictions.predicate.RestrictionValuePredicate.IGNORE_NULL_OR_EMPTY;
import static java.lang.Boolean.FALSE;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

import org.junit.Test;

import be.shad.tsqb.domain.House;
import be.shad.tsqb.restrictions.RestrictionsGroupFactory;
import be.shad.tsqb.test.TypeSafeQueryTest;
import be.shad.tsqb.values.HqlQueryValueImpl;

public class RestrictionChainingTest extends TypeSafeQueryTest {

    @Test
    public void testAnd() {
        House house = query.from(House.class);
        query.where(house.getFloors()).gt(4).and(house.isOccupied()).isFalse();
        validate(" from House hobj1 where hobj1.floors > :np1 and hobj1.occupied = :np2", 4, FALSE);
    }

    @Test
    public void testAndWithIgnored() {
        House house = query.from(House.class);
        query.where(house.getFloors()).gt(4).and(house.isOccupied()).eq(null, IGNORE_NULL).and(house.getName()).eq("Domus");
        validate(" from House hobj1 where hobj1.floors > :np1 and hobj1.name = :np2", 4, "Domus");
    }

    @Test
    public void testOr() {
        House house = query.from(House.class);
        query.where(house.getFloors()).gt(4).or(house.isOccupied()).isFalse();
        validate(" from House hobj1 where hobj1.floors > :np1 or hobj1.occupied = :np2", 4, FALSE);
    }

    @Test
    public void testOrWithIgnored() {
        House house = query.from(House.class);
        query.where(house.getFloors()).gt(4).or(house.isOccupied()).eq(null, IGNORE_NULL).or(house.getName()).eq("Domus");
        validate(" from House hobj1 where hobj1.floors > :np1 or hobj1.name = :np2", 4, "Domus");
    }
    
    @Test
    public void testAnds() {
        House house = query.from(House.class);
        query.where(house.getFloors()).gt(4).and(house.isOccupied()).isFalse().and(house.getPrice()).eq(ZERO);
        validate(" from House hobj1 where hobj1.floors > :np1 and hobj1.occupied = :np2 and hobj1.price = :np3", 4, FALSE, ZERO);
    }

    @Test
    public void testOrs() {
        House house = query.from(House.class);
        query.where(house.getFloors()).gt(4).or(house.isOccupied()).isFalse().or(house.getPrice()).eq(ZERO);
        validate(" from House hobj1 where hobj1.floors > :np1 or hobj1.occupied = :np2 or hobj1.price = :np3", 4, FALSE, ZERO);
    }

    @Test
    public void testGroups() {
        House house = query.from(House.class);
        
        // hopelessly complex grouping:
        query.where().and(group(query).
                and(house.getFloors()).gt(4).and(group(query).
                        and(house.isOccupied()).isFalse().
                        or(house.getPrice()).eq(ZERO)
                ).and(group(query).
                        and(house.getName()).startsWith("Cas").
                        or(house.getName()).startsWith("Chu")
                ));

        validate(" from House hobj1 where (hobj1.floors > :np1 and (hobj1.occupied = :np2 or hobj1.price = :np3) and (hobj1.name like :np4 or hobj1.name like :np5))", 
                4, FALSE, ZERO, "Cas%", "Chu%");
    }

    /**
     * Same as testGroups(), but with extra ignored restrictions
     */
    @Test
    public void testGroupsWithIgnores() {
        House house = query.from(House.class);
        RestrictionsGroupFactory gb = query.getGroupedRestrictionsBuilder();

        // hopelessly complex grouping:
        query.where().and(
            gb.where(house.getFloors()).gt(4).and(
                gb.where(house.isOccupied()).isFalse().or(house.getPrice()).eq(ZERO).and(house.getConstructionDate()).in(Collections.<Date>emptyList(), IGNORE_EMPTY_COLLECTION)
            ).and(
                gb.where(house.getName()).startsWith("Cas").and(house.getAddress().getStreet()).startsWith("", IGNORE_EMPTY_STRING).or(house.getName()).startsWith("Chu")
            ).and(
                gb.where(house.getPrice()).gt(null, IGNORE_NULL)
            ));

        validate(" from House hobj1 where (hobj1.floors > :np1 and (hobj1.occupied = :np2 or hobj1.price = :np3) and (hobj1.name like :np4 or hobj1.name like :np5))", 
                4, FALSE, ZERO, "Cas%", "Chu%");
    }

    /**
     * Same as testGroupsWithIgnores(), but with the ignores as default
     */
    @Test
    public void testGroupsWithDefaultIgnores() {
        House house = query.from(House.class);
        RestrictionsGroupFactory gb = query.getGroupedRestrictionsBuilder();
        query.setDefaultRestrictionValuePredicate(IGNORE_NULL_OR_EMPTY);
        
        // hopelessly complex grouping:
        query.where().and(
            gb.where(house.getFloors()).gt(4).and(
                gb.where(house.isOccupied()).isFalse().or(house.getPrice()).eq(ZERO).and(house.getConstructionDate()).in(Collections.<Date>emptyList())
            ).and(
                gb.where(house.getName()).startsWith("Cas").and(house.getAddress().getStreet()).startsWith("").or(house.getName()).startsWith("Chu")
            ).and(
                gb.where(house.getPrice()).gt((BigDecimal) null)
            ));

        validate(" from House hobj1 where (hobj1.floors > :np1 and (hobj1.occupied = :np2 or hobj1.price = :np3) and (hobj1.name like :np4 or hobj1.name like :np5))", 
                4, FALSE, ZERO, "Cas%", "Chu%");
    }
    
    @Test
    public void testWhereCustomHql() {
        query.from(House.class);
        query.where(new HqlQueryValueImpl("hobj1.floors > ?", 10));
        validate(" from House hobj1 where hobj1.floors > ?", 10);
    }
    
}
