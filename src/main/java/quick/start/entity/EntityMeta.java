package quick.start.entity;

import lombok.Data;
import quick.start.annotation.*;
import quick.start.util.AnnotationUtils;
import quick.start.util.FieldUtils;
import quick.start.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author yuanweiquan
 */
@Data
public class EntityMeta<E extends Entity> {

    /**
     * 对象类
     */
    private Class<E> entityClass;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 主键
     */
    private String primaryKey;
    /**
     * 自增key
     */
    private String generatedKey;
    /**
     * 插入的字段
     */
    private Set<String> insertFields = new HashSet<>();
    /**
     * @Column注解的字段映射：字段 -> 属性
     */
    private Map<String, String> columnMapping = new HashMap<>();
    /**
     * @Column注解的字段映射：属性 -> 字段
     */
    private Map<String, String> fieldMapping = new HashMap<>();

    private EntityMeta() {
    }

    public static <E extends Entity> EntityMeta<E> of(Class<E> entityClass) {
        EntityMeta<E> meta = new EntityMeta<>();
        meta.setEntityClass(entityClass);
        //解析表名称
        parserTableName(meta);
        //解析主键名称
        parserPrimary(meta);
        //解析自增key
        parserGeneratedKey(meta);
        //解析插入字段
        meta.parserInsertFields();
        //解析属性-字段映射关系
        meta.parserColumnMapping(entityClass);
        return meta;
    }

    /**
     * 解析表名称，@Table优先级最高，其次@MapUnderScoreToCamelCase，否则默认类名作为表名
     *
     * @param meta 类元数据对象
     * @param <E>
     */
    private static <E extends Entity> void parserTableName(EntityMeta<E> meta) {
        Class<E> entityClass = meta.getEntityClass();
        if (AnnotationUtils.isAnnotationPresent(entityClass, Table.class)) {
            meta.setTableName(entityClass.getAnnotation(Table.class).value());
            return;
        }
        if (AnnotationUtils.isAnnotationPresent(entityClass, MapUnderScoreToCamelCase.class)) {
            meta.setTableName(quick.start.util.StringUtils.humpToLine(entityClass.getSimpleName()));
            return;
        }
        if (StringUtils.isEmpty(meta.tableName)) {
            meta.setTableName(entityClass.getSimpleName());
        }
    }

    private void parserColumnMapping(Class<E> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (AnnotationUtils.isAnnotationPresent(field, Column.class, x -> !StringUtils.isEmpty(x.value()))) {
                String columnValue = AnnotationUtils.getAnnotationAttribute(field.getAnnotation(Column.class), x -> x.value());
                columnMapping.put(columnValue, field.getName());
                fieldMapping.put(field.getName(), columnValue);
            } else if (AnnotationUtils.isAnnotationPresent(entityClass, MapUnderScoreToCamelCase.class)) {
                String columnValue = StringUtils.humpToLine(field.getName());
                columnMapping.put(columnValue, field.getName());
                fieldMapping.put(field.getName(), columnValue);
            }
        }
    }

    private void parserInsertFields() {
        for (Field field : entityClass.getDeclaredFields()) {
            //@SaveAble(false)、@Generated、未序列化、接口类型不保存
            if (!(AnnotationUtils.isAnnotationPresent(field, SaveAble.class, x -> !x.value())
                    || AnnotationUtils.isAnnotationPresent(field, Generated.class)
                    || (!(field.getType() instanceof Serializable))
                    || field.getType().isInterface())) {
                insertFields.add(FieldUtils.getFieldName(entityClass, field));
            }
        }
    }

    /**
     * 解析自增key
     *
     * @param meta
     * @param <E>
     */
    private static <E extends Entity> void parserGeneratedKey(EntityMeta<E> meta) {
        for (Field field : meta.getEntityClass().getDeclaredFields()) {
            if (AnnotationUtils.isAnnotationPresent(field, Generated.class)) {
                meta.generatedKey = FieldUtils.getFieldName(meta.getEntityClass(), field);
            }
        }
    }

    /**
     * 解析主键
     *
     * @param meta
     * @param <E>
     */
    private static <E extends Entity> void parserPrimary(EntityMeta<E> meta) {
        Class<E> eClass = meta.getEntityClass();
        //类上的@Primary注解
        if (AnnotationUtils.isAnnotationPresent(eClass, PrimaryKey.class)) {
            meta.setPrimaryKey(eClass.getAnnotation(PrimaryKey.class).value());
            return;
        }
        for (Field field : eClass.getDeclaredFields()) {
            if (AnnotationUtils.isAnnotationPresent(field, PrimaryKey.class)) {
                //@Column注解优先级高
                if (AnnotationUtils.isAnnotationPresent(field, Column.class, x -> !StringUtils.isEmpty(x.value()))) {
                    meta.setPrimaryKey(field.getAnnotation(Column.class).value());
                    break;
                }
                //再解析@PrimaryKey
                String primaryKey = field.getAnnotation(PrimaryKey.class).value();
                if (!StringUtils.isEmpty(primaryKey)) {
                    meta.setPrimaryKey(field.getName());
                    break;
                }
                meta.setPrimaryKey(FieldUtils.getFieldName(eClass, field));
                break;
            }
        }
    }

}
