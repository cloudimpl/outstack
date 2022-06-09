package com.cloudimpl.outstack.repo.postgres;

import com.cloudimpl.rstack.dsl.restql.BinNode;
import com.cloudimpl.rstack.dsl.restql.ConstArrayNode;
import com.cloudimpl.rstack.dsl.restql.ConstBooleanArrayNode;
import com.cloudimpl.rstack.dsl.restql.ConstBooleanNode;
import com.cloudimpl.rstack.dsl.restql.ConstNode;
import com.cloudimpl.rstack.dsl.restql.ConstNumberArrayNode;
import com.cloudimpl.rstack.dsl.restql.ConstNumberNode;
import com.cloudimpl.rstack.dsl.restql.ConstStringArrayNode;
import com.cloudimpl.rstack.dsl.restql.ConstStringNode;
import com.cloudimpl.rstack.dsl.restql.FieldCheckNode;
import com.cloudimpl.rstack.dsl.restql.OrderByExpNode;
import com.cloudimpl.rstack.dsl.restql.OrderByNode;
import com.cloudimpl.rstack.dsl.restql.PlaceHolderNode;
import com.cloudimpl.rstack.dsl.restql.RelNode;
import com.cloudimpl.rstack.dsl.restql.RestQLNode;
import com.cloudimpl.rstack.dsl.restql.RestQLParser;
import com.google.gson.JsonObject;

import java.util.stream.Collectors;

/**
 *
 * @author nuwan
 */
public class PostgresSqlNode implements RestQLNode {
    private final String entityField;
    public PostgresSqlNode(String entityField)
    {
        this.entityField = entityField;
    }

    public PostgresSqlNode()
    {
        this("entity");
    }

    @Override
    public String eval(RestQLNode node) {
        if (node instanceof ConstArrayNode) {
            return ConstArrayNode.class.cast(node).getVals().toString();
        } else if (node instanceof ConstStringNode) {
            return String.valueOf(ConstStringNode.class.cast(node).getVal());
        } else if (node instanceof ConstNumberNode) {
            return String.valueOf(ConstNumberNode.class.cast(node).getVal());
        } else if (node instanceof ConstBooleanNode) {
            return String.valueOf(ConstBooleanNode.class.cast(node).getVal());
        }
        else if(node instanceof PlaceHolderNode)
        {
            return String.valueOf(PlaceHolderNode.class.cast(node).getVal());
        }
        else if (node instanceof RelNode) {
            RelNode rel = RelNode.class.cast(node);
            if (rel.getOp() == RelNode.Op.IN){
                return this.inOperator(rel);
            } else{
                return castToType(convertToJsonField(this.entityField,rel.getFieldName()), rel.getConstNode()) + (rel.getOp() == RelNode.Op.LIKE ? " ILIKE ":(rel.getOp() == RelNode.Op.NOT_LIKE?" NOT ILIKE ":rel.getOp().getOp())) + (String) rel.getConstNode().eval(this);
            }
        }else if(node instanceof FieldCheckNode) {
            FieldCheckNode fieldNode = FieldCheckNode.class.cast(node);
            return convertToJsonField(this.entityField,fieldNode.getFieldName()) + (fieldNode.isCheckExist() ? " is not null " : " is null ");
        } else if (node instanceof BinNode) {
            BinNode binNode = BinNode.class.cast(node);
            return "(" + binNode.getLeft().eval(this) + binNode.getOp().getOp() + binNode.getRight().eval(this) + ")";
        }else if (node instanceof OrderByNode) {
            OrderByNode orderBy = OrderByNode.class.cast(node);
            return castToType(convertToJsonField(this.entityField,orderBy.getFieldName()),orderBy.getDataType() )+ " " + orderBy.getOrder();
        }else if(node instanceof OrderByExpNode)
        {
            OrderByExpNode expNode = OrderByExpNode.class.cast(node);
            return expNode.getOrderByList().stream().map(i->(String)i.eval(this)).collect(Collectors.joining(","));
        }
        throw new RuntimeException("unknown node :" + node.getClass().getName());
    }

    @Override
    public JsonObject toJson() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String inOperator(RelNode relNode){
        ConstNode constNode = relNode.getConstNode();
        if (constNode instanceof ConstStringArrayNode){
            return convertToJsonField(this.entityField, relNode.getFieldName() ) + " IN (" + ConstStringArrayNode.class.cast(constNode).getVals().stream().collect(Collectors.joining(",")) + ")";
        } else if(constNode instanceof ConstNumberArrayNode){
            return convertToJsonField( this.entityField,relNode.getFieldName() ) + " IN (" + ConstNumberArrayNode.class.cast(constNode).getVals().stream().map(v->v.toString()).collect(Collectors.joining(",")) + ")";
        } else if(constNode instanceof ConstBooleanArrayNode){
            return convertToJsonField( this.entityField,relNode.getFieldName() ) + " IN (" + ConstBooleanArrayNode.class.cast(constNode).getVals().stream().map(v->v.toString()).collect(Collectors.joining(",")) + ")";
        } else {
            throw new RuntimeException("unknown const array node :" + constNode.getClass().getName());
        }
    }

    public static String convertToJsonField(String entityField,String field) {
        field = field.trim();
        if(field.startsWith("_"))
        {
            return field.substring(1);
        }

        String[] fields = field.split("\\.");
        if (fields.length == 1) {
            return entityField.concat("->>'") + field + "'";
        } else if (fields.length == 2) {
            return entityField.concat("->'") + fields[0] + "'->>'" + fields[1] + "'";
        } else if (fields.length > 2) {
            String param = entityField;
            for (int i = 0; i < fields.length; i++) {
                if (i == fields.length - 1) {
                    param = param + "->>'" + fields[i] + "'";
                } else {
                    param = param + "->'" + fields[i]+ "'";
                }
            }
            return param;
        }
        throw new RuntimeException("invalid field format");
    }

    public static String castToType(String fieldName, ConstNode constNode) {
        if (constNode instanceof ConstNumberNode) {
            return "(" + fieldName + ")::numeric";
        } else if (constNode instanceof ConstBooleanNode) {
            return "(" + fieldName + ")::bool";
        } else {
            return fieldName;
        }
    }

    public static String castToType(String fieldName,OrderByNode.DataType dataType) {
        switch(dataType)
        {
            case BOOL:
            {
                return "("+fieldName+")::bool";
            }
            case NUMBER:{
                return "("+fieldName+")::numeric";
            }
            case STRING:
            {
                return fieldName;
            }
            default:
            {
                return fieldName;
            }
        }

    }

    public static void main(String[] args) {
        RestQLNode node = RestQLParser.parse("_resourceType = 'com.restrata.db.repo.TestEntity'");
        PostgresSqlNode sqlNode = new PostgresSqlNode();
        System.out.println(sqlNode.eval(node));
    }
}