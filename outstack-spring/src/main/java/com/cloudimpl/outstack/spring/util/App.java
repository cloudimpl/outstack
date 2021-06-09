package com.cloudimpl.outstack.spring.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionCheck;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.Put;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ReturnValuesOnConditionCheckFailure;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItem;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItemsRequest;
import com.amazonaws.services.dynamodbv2.model.Update;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

/**
 * AWS DynamoDB Transaction Example for Java
 *
 */
public class App {

    public static void createMapTable(DynamoDB ddb, String tableName, String hashKeyName) {
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName(hashKeyName).withAttributeType("S"));
        //  attributeDefinitions.add(new AttributeDefinition().withAttributeName("Xvalue").withAttributeType("S"));

        List<KeySchemaElement> keySchema = new ArrayList<>();
        keySchema.add(new KeySchemaElement().withAttributeName(hashKeyName).withKeyType(KeyType.HASH));
        // keySchema.add(new KeySchemaElement().withAttributeName("Xvalue").withKeyType(KeyType.RANGE));

        CreateTableRequest request = new CreateTableRequest()
                .withTableName(tableName)
                .withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(5L)
                        .withWriteCapacityUnits(6L));

        ddb.createTable(request);

    }

    public static void main(String[] args) {
        Gson gson = new Gson();
        System.out.println(gson.toJson("Amazon DynamoDB Transaction Sample!"));

        //Using local profile(default)
        //AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        //Using custom profile
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                //   .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "US_EAST_1"))
                ///.withRegion(Regions.US_EAST_1)
                //.withCredentials(new ProfileCredentialsProvider("profile_test"))
                .build();
        DynamoDB dynamoDB = new DynamoDB(client);

        //   App.createMapTable(dynamoDB, "Customers", "CustomerId");
        //   App.createMapTable(dynamoDB, "ProductCatalog", "ProductId");
        //   App.createMapTable(dynamoDB, "Orders", "OrderId");
        TableCollection<ListTablesResult> tables = dynamoDB.listTables();
        Iterator<Table> iterator = tables.iterator();

        while (iterator.hasNext()) {
            Table table = iterator.next();
            System.out.println(table.getTableName());
        }

        //Create condition for "Customers" table
        final String CUSTOMER_TABLE_NAME = "Customers";
        final String CUSTOMER_PARTITION_KEY = "CustomerId";
        final String customerId = "09e8e9c8-ec48"; //Sample ID that is must created before running
        final HashMap<String, AttributeValue> customerItemKey = new HashMap<>();
        customerItemKey.put(CUSTOMER_PARTITION_KEY, new AttributeValue(customerId));

//        Put createCustomer = new Put()
//                .withTableName(CUSTOMER_TABLE_NAME)
//                .withItem(customerItemKey)
//                .withReturnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
//                .withConditionExpression("attribute_not_exists(" + CUSTOMER_PARTITION_KEY + ")");
//
//        Collection<TransactWriteItem> actions = Arrays.asList(
//                new TransactWriteItem().withPut(createCustomer));
//
//        TransactWriteItemsRequest createCustomerTransaction = new TransactWriteItemsRequest()
//                .withTransactItems(actions)
//                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
//
//        client.transactWriteItems(createCustomerTransaction);

        ConditionCheck checkItem = new ConditionCheck()
                .withTableName(CUSTOMER_TABLE_NAME)
                .withKey(customerItemKey)
                .withConditionExpression("attribute_exists(" + CUSTOMER_PARTITION_KEY + ")");

        System.out.println(gson.toJson(checkItem));
        
        //Create condition for "ProductCatalog" table
        final String PRODUCT_TABLE_NAME = "ProductCatalog";
        final String PRODUCT_PARTITION_KEY = "ProductId";
        final String productKey = "aaa-001"; //Product ID that is must inserted in the table before running
        HashMap<String, AttributeValue> productItemKey = new HashMap<>();
        productItemKey.put(PRODUCT_PARTITION_KEY, new AttributeValue(productKey));

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":new_status", new AttributeValue("SOLD"));
        expressionAttributeValues.put(":expected_status", new AttributeValue("IN_STOCK"));

        Put createProduct = new Put()
                .withTableName(PRODUCT_TABLE_NAME)
                .withItem(productItemKey)
                .withReturnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
                .withConditionExpression("attribute_not_exists(" + PRODUCT_PARTITION_KEY + ")");

        Collection<TransactWriteItem> actions2 = Arrays.asList(
                new TransactWriteItem().withPut(createProduct));

        TransactWriteItemsRequest createProductTransaction = new TransactWriteItemsRequest()
                .withTransactItems(actions2)
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);

        client.transactWriteItems(createProductTransaction);
        
        Update markItemSold = new Update()
                .withTableName(PRODUCT_TABLE_NAME)
                .withKey(productItemKey)
                .withUpdateExpression("SET ProductStatus = :new_status") //Status ID that is must inserted in the table before running, and have to set "IN_STOCK"
                .withExpressionAttributeValues(expressionAttributeValues)
                .withConditionExpression("ProductStatus = :expected_status")
                .withReturnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD);

        System.out.println(gson.toJson(markItemSold));

        //Create condition for "Orders" table
        final String ORDER_PARTITION_KEY = "OrderId";
        final String ORDER_TABLE_NAME = "Orders";
        final String orderId = "ord-001";
        HashMap<String, AttributeValue> orderItem = new HashMap<>();
        orderItem.put(ORDER_PARTITION_KEY, new AttributeValue(orderId));
        orderItem.put(PRODUCT_PARTITION_KEY, new AttributeValue(productKey));
        orderItem.put(CUSTOMER_PARTITION_KEY, new AttributeValue(customerId));
        orderItem.put("OrderStatus", new AttributeValue("CONFIRMED"));
        orderItem.put("OrderTotal", new AttributeValue("100"));

        Put createOrder = new Put()
                .withTableName(ORDER_TABLE_NAME)
                .withItem(orderItem)
                .withReturnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
                .withConditionExpression("attribute_not_exists(" + ORDER_PARTITION_KEY + ")");

        System.out.println(gson.toJson(createOrder));

        //Create a transaction with conditions
        Collection<TransactWriteItem> actions3 = Arrays.asList(
                new TransactWriteItem().withConditionCheck(checkItem),
                new TransactWriteItem().withPut(createOrder),
                new TransactWriteItem().withUpdate(markItemSold));

        TransactWriteItemsRequest placeOrderTransaction = new TransactWriteItemsRequest()
                .withTransactItems(actions3)
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);

        // Execute the transaction and process the result.
        try {
            client.transactWriteItems(placeOrderTransaction);
            System.out.println("Transaction Successful");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
//        catch (ResourceNotFoundException rnf) {
//            System.err.println("One of the table involved in the transaction is not found" + rnf.getMessage());
//        } catch (InternalServerErrorException ise) {
//            System.err.println("Internal Server Error" + ise.getMessage());
//        } catch (TransactionCanceledException tce) {
//            System.out.println("Transaction Canceled " + tce.getMessage());
//        }
    }
}
