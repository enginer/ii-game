package org.ayfaar.game.dao.impl;

import org.ayfaar.game.dao.CommonDao;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.apache.commons.beanutils.PropertyUtils.setProperty;
import static org.ayfaar.game.utils.EntityUtils.getPrimaryKeyFiledName;
import static org.ayfaar.game.utils.EntityUtils.getPrimaryKeyValue;
import static org.hibernate.criterion.Restrictions.eq;

@Repository
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class CommonDaoImpl implements CommonDao {

    @Autowired
    private SessionFactory sessionFactory;


    @Override
    public <E> List<E> getAll(Class<E> clazz) {
        return list(sessionFactory.getCurrentSession().createCriteria(clazz));
    }

    @Override
    public <E> E getRandom(Class<E> clazz) {
        return (E) sessionFactory.getCurrentSession().createCriteria(clazz)
            .add(Restrictions.sqlRestriction("1=1 order by rand()"))
            .setMaxResults(1)
            .list().get(0);
    }

    @Nullable
    @Override
    public <E> E get(Class<E> clazz, Serializable id) {
        return (E) sessionFactory.getCurrentSession().get(clazz, id);
    }

    @Override
    public <E> E get(Class<E> clazz, String property, Object value) {
        return (E) sessionFactory.getCurrentSession()
                .createCriteria(clazz).add(Restrictions.eq(property, value))
                .uniqueResult();
    }

    @Override
    public <E> List<E> getFor(Class<E> clazz, String entity, Serializable id) {
        return list(sessionFactory.getCurrentSession().createCriteria(clazz)
                .createAlias(entity, entity)
                .add(eq(entity+".id", id)));
    }

    @Override
    public <E> E getSingleFor(Class<E> clazz, String entity, Serializable id) {
        return (E) sessionFactory.getCurrentSession().createCriteria(clazz)
                .createAlias(entity, entity)
                .add(eq(entity + ".id", id))
                .uniqueResult();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public <E> E save(Class<E> clazz, E entity) {
        try {
            Object id = getPrimaryKeyValue(entity);
            if (id != null && id.equals(new Integer(0))) {
                setProperty(entity, getPrimaryKeyFiledName(clazz), null);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        sessionFactory.getCurrentSession().saveOrUpdate(entity);
        return entity;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public <E> E save(E entity) {
        return save((Class<E>) entity.getClass(), entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void remove(Class<?> entityClass, Serializable id) {
        Object entity = get(entityClass, id);
        sessionFactory.getCurrentSession().delete(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void remove(Object entity) {
        remove(entity.getClass(), (Serializable) getPrimaryKeyValue(entity));
    }

    @Override
    public <E> E getByCode(Class<E> className, String code) {
        return (E) sessionFactory.getCurrentSession()
                .createCriteria(className)
                .add(Restrictions.eq("code", code))
                .uniqueResult();
    }

    protected <E> List<E> list(Criteria criteria) {
        return new ArrayList<E>(new LinkedHashSet<E>(criteria.list())); // privent duplications
    }
}