/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.repo;

import com.cloudimpl.rstack.dsl.restql.*;
import com.google.gson.JsonObject;
import java.util.stream.Collectors;

/**
 *
 * @author nuwan
 */
public class PostgresSqlNode implements RestQLNode {

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
        } else if (node instanceof RelNode) {
            RelNode rel = RelNode.class.cast(node);
            if (rel.getOp() == RelNode.Op.IN){
                return this.inOperator(rel);
            } else{
                return castToType(convertToJsonField(rel.getFieldName()), rel.getConstNode()) + (rel.getOp() == RelNode.Op.LIKE ? " ILIKE ":(rel.getOp() == RelNode.Op.NOT_LIKE?" NOT ILIKE ":rel.getOp().getOp())) + (String) rel.getConstNode().eval(this);
            }
        }else if(node instanceof FieldCheckNode) {
            FieldCheckNode fieldNode = FieldCheckNode.class.cast(node);
            return convertToJsonField(fieldNode.getFieldName()) + (fieldNode.isCheckExist() ? " is not null " : " is null ");
        } else if (node instanceof BinNode) {
            BinNode binNode = BinNode.class.cast(node);
            return "(" + binNode.getLeft().eval(this) + binNode.getOp().getOp() + binNode.getRight().eval(this) + ")";
        }else if (node instanceof OrderByNode) {
            OrderByNode orderBy = OrderByNode.class.cast(node);
            return castToType(convertToJsonField(orderBy.getFieldName()),orderBy.getDataType() )+ " " + orderBy.getOrder();
        }else if(node instanceof  OrderByExpNode)
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
            return convertToJsonField( relNode.getFieldName() ) + " IN (" + ConstStringArrayNode.class.cast(constNode).getVals().stream().collect(Collectors.joining(",")) + ")";
        } else if(constNode instanceof ConstNumberArrayNode){
            return convertToJsonField( relNode.getFieldName() ) + " IN (" + ConstNumberArrayNode.class.cast(constNode).getVals().stream().map(v->v.toString()).collect(Collectors.joining(",")) + ")";
        } else if(constNode instanceof ConstBooleanArrayNode){
            return convertToJsonField( relNode.getFieldName() ) + " IN (" + ConstBooleanArrayNode.class.cast(constNode).getVals().stream().map(v->v.toString()).collect(Collectors.joining(",")) + ")";
        } else {
            throw new RuntimeException("unknown const array node :" + constNode.getClass().getName());
        }
    }

    public static String convertToJsonField(String field) {
        field = field.trim();
        if(field.equals("_eventType"))
        {
            return field.substring(1);
        }
        
        String[] fields = field.split("\\.");
        if (fields.length == 1) {
            return "json->>'" + field + "'";
        } else if (fields.length == 2) {
            return "json->'" + fields[0] + "'->>'" + fields[1] + "'";
        } else if (fields.length > 2) {
            String param = "json";
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
        OrderByExpNode node = RestQLParser.parseOrderBy("N(seqNum):DESC");
        PostgresSqlNode postgersNode = new PostgresSqlNode();
        String sql = postgersNode.eval(node);
        System.out.println("sql : "+sql);

        RestQLNode parse = RestQLParser.parse("read=false");
        PostgresSqlNode postgersNode2 = new PostgresSqlNode();
        String sql2 = postgersNode2.eval(parse);
        System.out.println("sql2 : "+sql2);
    }
}
