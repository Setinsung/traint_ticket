package com.hdu.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hdu.exception.ParamException;
import org.apache.commons.collections.MapUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.*;

public class BeanValidator {

    private static ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    /**
     * 验证单个对象的方法
     *
     * @param t      要验证的对象
     * @param groups 验证分组
     * @param <T>    对象的类型
     * @return 包含验证错误信息的 Map，如果验证通过则返回空 Map
     */
    public static <T> Map<String, String> validate(T t, Class... groups) {
        Validator validator = validatorFactory.getValidator();
        Set validateResult = validator.validate(t, groups);
        if (validateResult.isEmpty()) {
            return Collections.emptyMap();
        } else {
            LinkedHashMap errors = Maps.newLinkedHashMap();
            Iterator iterator = validateResult.iterator();
            while (iterator.hasNext()) {
                ConstraintViolation violation = (ConstraintViolation)iterator.next();
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            return errors;
        }
    }


    /**
     * 验证对象集合的方法
     *
     * @param collection 包含要验证对象的集合
     * @return 包含验证错误信息的 Map，如果验证通过则返回空 Map
     */
    public static Map<String, String> validateList(Collection<?> collection) {
        Preconditions.checkNotNull(collection);
        Iterator iterator = collection.iterator();
        Map errors;

        do {
            if (!iterator.hasNext()) {
                return Collections.emptyMap();
            }
            Object object = iterator.next();
            errors = validate(object, new Class[0]);
        } while (errors.isEmpty());

        return errors;
    }


    /**
     * 验证多个对象的方法
     *
     * @param first   第一个要验证的对象
     * @param objects 其他要验证的对象
     * @return 包含验证错误信息的 Map，如果验证通过则返回空 Map
     */
    public static Map<String, String> validateObject(Object first, Object... objects) {
        if (objects != null && objects.length > 0) {
            return validateList(Lists.asList(first, objects));
        } else {
            return validate(first, new Class[0]);
        }
    }

    /**
     * 检查对象是否满足验证规则的方法，不满足则抛出 ParamException 异常
     *
     * @param param 要验证的对象
     * @throws ParamException 如果验证不通过，则抛出 ParamException 异常
     */
    public static void check(Object param) throws ParamException {
        Map<String, String> map = BeanValidator.validateObject(param);
        if (MapUtils.isNotEmpty(map)) {
            throw new ParamException(map.toString());
        }
    }
}
