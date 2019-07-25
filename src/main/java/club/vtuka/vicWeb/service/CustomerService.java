package club.vtuka.vicWeb.service;

import club.vtuka.vicWeb.helper.DataBaseHelper;
import club.vtuka.vicWeb.model.Customer;
import club.vtuka.vicWeb.util.PropsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CustomerService {
    private static Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

    public List<Customer> getCustomerList(){
        String sql = "select * from customer";
        return DataBaseHelper.queryEntityList(Customer.class, sql);
    }

    public Customer getCustomer(Long id){
        String sql = "select * from customer where id = ?";
        return DataBaseHelper.queryEntity(Customer.class,sql,id);
    }

    public boolean createCustomer(Map<String,Object> fieldMap){
        return DataBaseHelper.insertEntity(Customer.class,fieldMap);
    }

    public boolean updateCustomer(Long id,Map<String, Object> fieldMap){
        return DataBaseHelper.updateEntity(Customer.class,id,fieldMap);
    }

    public boolean deleteCustomer(Long id){
        return DataBaseHelper.deleteEntity(Customer.class,id);
    }



}
