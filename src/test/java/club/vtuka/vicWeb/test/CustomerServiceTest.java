package club.vtuka.vicWeb.test;

import club.vtuka.vicWeb.helper.DataBaseHelper;
import club.vtuka.vicWeb.model.Customer;
import club.vtuka.vicWeb.service.CustomerService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

public class CustomerServiceTest {

    private  final CustomerService customerService;

    public CustomerServiceTest(){
        customerService = new CustomerService();
    }

    @Before
    public void init() throws IOException {
        String file = "sql/customer_init.sql";
        DataBaseHelper.executeSqlFile(file);
    }

    @Test
    public void getCustomerListTest() {
        List<Customer> customerList = customerService.getCustomerList();
        Assert.assertEquals(2,customerList.size());
    }

    @Test
    public void getCustomerTest(){
        Long id = 1L;
        Customer customer = customerService.getCustomer(id);
        Assert.assertNotNull(customer);
    }

    @Test
    public void createCustomerTest(){
        HashMap<String, Object> fieldMap = new HashMap<>(1);
        fieldMap.put("name","customer999");
        fieldMap.put("contact","Test");
        fieldMap.put("telephone","13111120705");
        boolean customer = customerService.createCustomer(fieldMap);
        Assert.assertTrue(customer);
    }

    @Test
    public void updateCutomerTest(){
        Long id = 1L;
        HashMap<String, Object> fieldMap = new HashMap<>(1);
        fieldMap.put("contact","Abby");
        boolean b = customerService.updateCustomer(id, fieldMap);
        Assert.assertTrue(b);
    }

    @Test
    public void deleteCustomerTest(){
        Long id = 1L;
        boolean b = customerService.deleteCustomer(id);
        Assert.assertTrue(b);
    }


}
