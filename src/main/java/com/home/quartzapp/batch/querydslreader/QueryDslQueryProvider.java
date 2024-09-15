package com.home.quartzapp.batch.querydslreader;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.Query;
import org.springframework.batch.item.database.orm.AbstractJpaQueryProvider;

import java.util.function.Function;

class QueryDslQueryProvider<T> extends AbstractJpaQueryProvider {
    private Function<JPAQueryFactory, JPAQuery<T>> queryFunction;

    public QueryDslQueryProvider(Function<JPAQueryFactory, JPAQuery<T>> queryFunction) {
        this.queryFunction = queryFunction;
    }
    @Override
    public Query createQuery() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(getEntityManager());
        return queryFunction.apply(queryFactory).createQuery();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
