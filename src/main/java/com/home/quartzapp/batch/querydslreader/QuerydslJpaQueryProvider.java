package com.home.quartzapp.batch.querydslreader;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.Query;
import org.springframework.batch.item.database.orm.AbstractJpaQueryProvider;

import java.util.function.Function;

class QuerydslJpaQueryProvider<T> extends AbstractJpaQueryProvider {
    private final JPAQueryFactory jpaQueryFactory;
    private Function<JPAQueryFactory, JPAQuery<T>> queryFunction = null;

    public QuerydslJpaQueryProvider() {
        jpaQueryFactory = new JPAQueryFactory(getEntityManager());
    }

    public QuerydslJpaQueryProvider(Function<JPAQueryFactory, JPAQuery<T>> queryFunction) {
        jpaQueryFactory = new JPAQueryFactory(getEntityManager());
        this.queryFunction = queryFunction;
    }

    public void setJpaQuery(Function<JPAQueryFactory, JPAQuery<T>> queryFunction) {
        this.queryFunction = queryFunction;
    }
    
    @Override
    public Query createQuery() {
        return queryFunction.apply(jpaQueryFactory).createQuery();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
