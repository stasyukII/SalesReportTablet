
import org.json.*;
import org.junit.Assert;
import org.junit.Test;

import java.sql.*;

public class TestMachine {

    //  Database credentials
    static final String DB_URL = "jdbc:postgresql://localhost/postgres";
    static final String USER = "postgres";
    static final String PASS = "1a2a3a4a";
    static String template;
    static Connection connection;

    public static void main(String[] argv) throws Exception {
        TestMachine machine = new TestMachine();
        System.out.println("Testing connection to PostgreSQL JDBC");
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path ");
            e.printStackTrace();
            return;
        }

        System.out.println("PostgreSQL JDBC Driver successfully connected");

        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);

        } catch (SQLException e) {
            System.out.println("Connection Failed");
            e.printStackTrace();
            return;
        }

        if (connection != null) {
            System.out.println("You successfully connected to database now");
        } else {
            System.out.println("Failed to make connection to database");
        }
        /*
        machine.makeTable("itemsJson");
        machine.addItem("Nokia");
        machine.makePurchase("Nokia", 2, 8000, "2019-10-4");
        machine.demand("Nokia", 1, 20000, "2019-10-4");
        machine.makePurchase("Nokia",3,8000,"2019-10-5");
        machine.salesreport("Nokia", "2019-10-4");
        machine.clearTablet();
        */
    }

    // Делаем таблицу
    public void makeTable(String template) {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);

        } catch (SQLException e) {
            System.out.println("Connection Failed");
            e.printStackTrace();
            return;
        }
        String sql = "CREATE TABLE IF NOT EXISTS " + template + " (\n"
                + " item text UNIQUE NOT NULL , \n"
                + " quantity integer NOT NULL DEFAULT 0, \n"
                + "  data json DEFAULT '{}'\n);";
        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            System.out.println("You cant use this name");
            e.printStackTrace();
        }
        this.template = template;
        System.out.println("Successfully made template named - " + template);
    }

    //Добавляем продукт в таблицу
    public void addItem(String item) {
        String sql = "INSERT INTO " + template + " (item) \n"
                + "VALUES ('" + item.toLowerCase() + "')";
        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);
            System.out.println("Item added to " + template);
        } catch (SQLException e) {
            System.out.println("Name already exist");
            e.printStackTrace();
            return;
        }

    }
    @Test
    public void testAddItem(){
        TestMachine testMachine = new TestMachine();
        testMachine.makeTable("junit");
        testMachine.addItem("Phone");
        testMachine.addItem("Chair");
        testMachine.addItem("car");
        testMachine.addItem("house");
        int i =0;
        try{
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT item FROM " + template);
        while(rs.next()){
            i++;
        }
        }

        catch (SQLException e){
            e.printStackTrace();
        }
        Assert.assertEquals(4,i);
        testMachine.clearTablet();
    }

    //Закупка
    public void makePurchase(String item, int quantity, int price, String date) throws Exception {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);

        } catch (SQLException e) {
            System.out.println("Connection Failed");
            e.printStackTrace();
            return;
        }
        Statement statement = connection.createStatement();
        //Проверяем есть ли данный товар
        try {
            ResultSet rs = statement.executeQuery("SELECT item FROM " + template + " WHERE item ='" + item.toLowerCase() + "'");
            if (!rs.next()) {
                throw new IllegalArgumentException("There's no such item");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        //проверяем корректность цены и кол-ва товара
        if (quantity <= 0 || price <= 0) {
            throw new IllegalArgumentException("Quantities and prices cant be less or equal 0");
        }
        //Приводим строку к формату даты
        // Date dateSQL = null;
        // dateSQL = dateSQL.valueOf(date);
        ResultSet rs = statement.executeQuery("SELECT data FROM " + template + " WHERE item ='" + item.toLowerCase() + "'");
        String json = "'[{\"buy\":\"" + date + "\"},{\"price\": " + price + ",\"quantity\": " + quantity + "}]'";
        String sql = "UPDATE " + template + " SET \n"
                + "data=" + json + "::jsonb || data::jsonb,\n"
                + "quantity=quantity +" + quantity + "\n"
                + "WHERE item = '" + item.toLowerCase() + "'";
        try {
            statement.execute(sql);
            System.out.println("Added purchase on date " + date);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }
    @Test
    public void testPurchase()throws Exception{
        TestMachine testMachine = new TestMachine();
        testMachine.makeTable("junit");
        testMachine.addItem("Phone");
        testMachine.addItem("Chair");
        testMachine.addItem("car");
        testMachine.addItem("house");
        testMachine.makePurchase("Phone",3,20000,"2019-10-5");
        testMachine.makePurchase("Chair",8,500,"2018-6-24");
        testMachine.makePurchase("car",2,5000000,"2015-1-1");
        testMachine.makePurchase("house",1,45000000,"2010-5-26");
        int i =0;
        try{
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT data FROM " + template);
            while(rs.next()){
                i++;
            }
        }

        catch (SQLException e){
            e.printStackTrace();
        }
        Assert.assertEquals(4,i);
        testMachine.clearTablet();
    }



    public void demand(String item, int quantity, int price, String date) throws Exception {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);

        } catch (SQLException e) {
            System.out.println("Connection Failed");
            e.printStackTrace();
            return;
        }
        Statement statement = connection.createStatement();
        //Date dateSQL = null;
        //dateSQL = dateSQL.valueOf(date);
        try {
            ResultSet rs = statement.executeQuery("SELECT item FROM " + template + " WHERE item ='" + item.toLowerCase() + "'");
            if (!rs.next()) {
                throw new IllegalArgumentException("There's no such item");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        //проверяем корректность цены и кол-ва товара
        if (price <= 0) {
            throw new IllegalArgumentException("Prices cant be less or equal 0");
        }
        try {
            ResultSet rs = statement.executeQuery("SELECT quantity as qty FROM " + template + " WHERE item ='" + item.toLowerCase() + "'");
            if (rs.next() && rs.getInt("qty") < quantity) {
                throw (new IllegalArgumentException("You cant sell more then you have"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        String json = "'[{\"sell\":\"" + date + "\"},{\"price\": " + price + ",\"quantity\": " + quantity + "}]'";
        String sql = "UPDATE " + template + " SET \n"
                + "data=" + json + "::jsonb || data::jsonb, \n"
                + "quantity=quantity -" + quantity + "\n"
                + "WHERE item = '" + item.toLowerCase() + "'";
        try {
            statement.execute(sql);
            System.out.println("Added sale on date " + date);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }
    @Test
    public void testDemand()throws Exception{
        TestMachine testMachine = new TestMachine();
        testMachine.makeTable("junit");
        testMachine.addItem("Phone");
        testMachine.addItem("Chair");
        testMachine.addItem("car");
        testMachine.addItem("house");
        testMachine.makePurchase("Phone",3,20000,"2019-10-5");
        testMachine.makePurchase("Chair",8,500,"2018-6-24");
        testMachine.makePurchase("car",2,5000000,"2015-1-1");
        testMachine.makePurchase("house",1,45000000,"2010-5-26");
        testMachine.demand("Phone",3,20000,"2019-10-5");
        testMachine.demand("Chair",8,500,"2018-6-24");
        testMachine.demand("car",2,5000000,"2015-1-1");
        testMachine.demand("house",1,45000000,"2010-5-26");
        int i =0;
        try{
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT data FROM " + template);
            while(rs.next()){
                i++;
            }
        }

        catch (SQLException e){
            e.printStackTrace();
        }
        Assert.assertEquals(4,i);
        testMachine.clearTablet();
    }
    public int salesreport(String item, String date) throws Exception {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);

        } catch (SQLException e) {
            System.out.println("Connection Failed");
            e.printStackTrace();
            return 0;
        }
        Statement statement = connection.createStatement();
        //Date dateSQL = null;
        //dateSQL = dateSQL.valueOf(date);
        int buyingPrice = 0;
        int sellingPrice = 0;
        int quantity = 0;
        JSONArray arr = new JSONArray();
        try {
            ResultSet rs = statement.executeQuery("SELECT item FROM " + template + " WHERE item ='" + item.toLowerCase() + "'");
            if (!rs.next()) {
                throw new IllegalArgumentException("There's no such item");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ResultSet rs = statement.executeQuery("SELECT json_array_elements(data) FROM " + template + " WHERE item ='" + item.toLowerCase() + "'");
        while (rs.next()) {
            JSONObject obj = new JSONObject(rs.getString(1));
            arr.put(obj);

        }
        //парсим jsonarray
        for (int i = 0; i < arr.length(); i=i+2) {
            if (arr.getJSONObject(i).has("sell")){
            if (arr.getJSONObject(i).getString("sell").equals(date)) {
                sellingPrice = (arr.getJSONObject(i + 1).getInt("price"));
                quantity = quantity+(arr.getJSONObject(i + 1).getInt("quantity"));
                System.out.println(sellingPrice+" "+quantity);
            }
            }
            if (arr.getJSONObject(i).has("buy")){
            if (arr.getJSONObject(i).getString("buy").equals(date)) {
                buyingPrice = (arr.getJSONObject(i + 1).getInt("price"));
            }
            }
        }
        System.out.println("Salesreport done. Profit = "+((sellingPrice - buyingPrice) * quantity));
        return (sellingPrice - buyingPrice) * quantity;
    }
    public void clearTablet(){
        try {
            Statement statement = connection.createStatement();
            statement.execute("TRUNCATE "+ template);
            System.out.println("Template successfully cleared");
        } catch (SQLException e) {
            System.out.println("You cant use this name");
            e.printStackTrace();
        }
    }
    @Test
    public void testSales()throws Exception{
        TestMachine testMachine = new TestMachine();
        testMachine.makeTable("junit");
        testMachine.addItem("Phone");
        testMachine.addItem("Chair");
        testMachine.addItem("car");
        testMachine.addItem("house");
        testMachine.makePurchase("Phone",3,20000,"2019-10-5");
        testMachine.makePurchase("Chair",8,500,"2018-6-24");
        testMachine.makePurchase("car",2,5000000,"2015-1-1");
        testMachine.makePurchase("house",1,45000000,"2010-5-26");
        testMachine.demand("Phone",3,50000,"2019-10-5");
        testMachine.demand("Chair",8,1000,"2018-6-24");
        testMachine.demand("car",2,6000000,"2015-1-1");
        testMachine.demand("house",1,50000000,"2010-5-26");
        int sale1 = testMachine.salesreport("Phone","2019-10-5");
        int sale2 = testMachine.salesreport("Chair","2018-6-24");
        int sale3 = testMachine.salesreport("car","2015-1-1");
        int sale4 = testMachine.salesreport("house","2010-5-26");
        Assert.assertEquals(90000,sale1);
        Assert.assertEquals(4000,sale2);
        Assert.assertEquals(2000000,sale3);
        Assert.assertEquals(5000000,sale4);
        testMachine.clearTablet();
    }
}


