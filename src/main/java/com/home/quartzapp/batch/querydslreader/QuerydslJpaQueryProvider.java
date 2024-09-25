package com.home.quartzapp.batch.querydslreader;

import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.Query;
import org.springframework.batch.item.database.orm.AbstractJpaQueryProvider;

import java.util.function.Function;

class QuerydslJpaQueryProvider<T> extends AbstractJpaQueryProvider {
    private Function<JPAQueryFactory, JPAQuery<T>> queryFunction = null;

    public QuerydslJpaQueryProvider() {
    }

    public QuerydslJpaQueryProvider(Function<JPAQueryFactory, JPAQuery<T>> queryFunction) {
        this.queryFunction = queryFunction;
    }

    public void setJpaQuery(Function<JPAQueryFactory, JPAQuery<T>> queryFunction) {
        this.queryFunction = queryFunction;
    }
    
    @Override
    public Query createQuery() {
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(JPQLTemplates.DEFAULT, getEntityManager());
        return queryFunction.apply(jpaQueryFactory).createQuery();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
