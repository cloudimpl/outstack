/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.repo;

import com.cloudimpl.rstack.dsl.restql.BinNode;
import com.cloudimpl.rstack.dsl.restql.ConstArrayNode;
import com.cloudimpl.rstack.dsl.restql.ConstBooleanNode;
import com.cloudimpl.rstack.dsl.restql.ConstNumberNode;
import com.cloudimpl.rstack.dsl.restql.ConstStringNode;
import com.cloudimpl.rstack.dsl.restql.OrderByExpNode;
import com.cloudimpl.rstack.dsl.restql.OrderByNode;
import com.cloudimpl.rstack.dsl.restql.RelNode;
import com.cloudimpl.rstack.dsl.restql.RestQLNode;
import com.cloudimpl.rstack.dsl.restql.ConstNode;
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
            return castToType(convertToJsonField(rel.getFieldName()), rel.getConstNode()) + (rel.getOp() == RelNode.Op.LIKE ? " ILIKE ":rel.getOp().getOp()) + (String) rel.getConstNode().eval(this);
        } else if (node instanceof BinNode) {
            BinNode binNode = BinNode.class.cast(node);
            return "(" + binNode.getLeft().eval(this) + binNode.getOp().getOp() + binNode.getRight().eval(this) + ")";
        }else if (node instanceof OrderByNode) {
            OrderByNode orderBy = OrderByNode.class.cast(node);
            return convertToJsonField(orderBy.getFieldName()) + " " + orderBy.getOrder();
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

    public static String convertToJsonField(String field) {
        String[] fields = field.split("\\.");
        if (fields.length == 1) {
            return "json->>'" + field + "'";
        } else if (fields.length == 2) {
            return "json->'" + fields[0] + "'->>'" + fields[1] + "'";
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
}
