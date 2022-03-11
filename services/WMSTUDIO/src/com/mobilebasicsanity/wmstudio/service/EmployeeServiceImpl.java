/*Copyright (c) 2019-2020 wavemaker.com All Rights Reserved.
 This software is the confidential and proprietary information of wavemaker.com You shall not disclose such Confidential Information and shall use it only in accordance
 with the terms of the source code license agreement you entered into with wavemaker.com*/
package com.mobilebasicsanity.wmstudio.service;

/*This is a Studio Managed File. DO NOT EDIT THIS FILE. Your changes may be reverted by Studio.*/

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.wavemaker.commons.InvalidInputException;
import com.wavemaker.commons.MessageResource;
import com.wavemaker.runtime.data.annotations.EntityService;
import com.wavemaker.runtime.data.dao.WMGenericDao;
import com.wavemaker.runtime.data.exception.EntityNotFoundException;
import com.wavemaker.runtime.data.export.DataExportOptions;
import com.wavemaker.runtime.data.export.ExportType;
import com.wavemaker.runtime.data.expression.QueryFilter;
import com.wavemaker.runtime.data.model.AggregationInfo;
import com.wavemaker.runtime.file.model.Downloadable;

import com.mobilebasicsanity.wmstudio.Employee;


/**
 * ServiceImpl object for domain model class Employee.
 *
 * @see Employee
 */
@Service("WMSTUDIO.EmployeeService")
@Validated
@EntityService(entityClass = Employee.class, serviceId = "WMSTUDIO")
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeServiceImpl.class);


    @Autowired
    @Qualifier("WMSTUDIO.EmployeeDao")
    private WMGenericDao<Employee, Integer> wmGenericDao;

    @Autowired
    @Qualifier("wmAppObjectMapper")
    private ObjectMapper objectMapper;


    public void setWMGenericDao(WMGenericDao<Employee, Integer> wmGenericDao) {
        this.wmGenericDao = wmGenericDao;
    }

    @Transactional(value = "WMSTUDIOTransactionManager")
    @Override
    public Employee create(Employee employee) {
        LOGGER.debug("Creating a new Employee with information: {}", employee);

        Employee employeeCreated = this.wmGenericDao.create(employee);
        // reloading object from database to get database defined & server defined values.
        return this.wmGenericDao.refresh(employeeCreated);
    }

    @Transactional(readOnly = true, value = "WMSTUDIOTransactionManager")
    @Override
    public Employee getById(Integer employeeId) {
        LOGGER.debug("Finding Employee by id: {}", employeeId);
        return this.wmGenericDao.findById(employeeId);
    }

    @Transactional(readOnly = true, value = "WMSTUDIOTransactionManager")
    @Override
    public Employee findById(Integer employeeId) {
        LOGGER.debug("Finding Employee by id: {}", employeeId);
        try {
            return this.wmGenericDao.findById(employeeId);
        } catch (EntityNotFoundException ex) {
            LOGGER.debug("No Employee found with id: {}", employeeId, ex);
            return null;
        }
    }

    @Transactional(readOnly = true, value = "WMSTUDIOTransactionManager")
    @Override
    public List<Employee> findByMultipleIds(List<Integer> employeeIds, boolean orderedReturn) {
        LOGGER.debug("Finding Employees by ids: {}", employeeIds);

        return this.wmGenericDao.findByMultipleIds(employeeIds, orderedReturn);
    }


    @Transactional(rollbackFor = EntityNotFoundException.class, value = "WMSTUDIOTransactionManager")
    @Override
    public Employee update(Employee employee) {
        LOGGER.debug("Updating Employee with information: {}", employee);

        this.wmGenericDao.update(employee);
        this.wmGenericDao.refresh(employee);

        return employee;
    }

    @Transactional(value = "WMSTUDIOTransactionManager")
    @Override
    public Employee partialUpdate(Integer employeeId, Map<String, Object>employeePatch) {
        LOGGER.debug("Partially Updating the Employee with id: {}", employeeId);

        Employee employee = getById(employeeId);

        try {
            ObjectReader employeeReader = this.objectMapper.reader().forType(Employee.class).withValueToUpdate(employee);
            employee = employeeReader.readValue(this.objectMapper.writeValueAsString(employeePatch));
        } catch (IOException ex) {
            LOGGER.debug("There was a problem in applying the patch: {}", employeePatch, ex);
            throw new InvalidInputException("Could not apply patch",ex);
        }

        employee = update(employee);

        return employee;
    }

    @Transactional(value = "WMSTUDIOTransactionManager")
    @Override
    public Employee delete(Integer employeeId) {
        LOGGER.debug("Deleting Employee with id: {}", employeeId);
        Employee deleted = this.wmGenericDao.findById(employeeId);
        if (deleted == null) {
            LOGGER.debug("No Employee found with id: {}", employeeId);
            throw new EntityNotFoundException(MessageResource.create("com.wavemaker.runtime.entity.not.found"), Employee.class.getSimpleName(), employeeId);
        }
        this.wmGenericDao.delete(deleted);
        return deleted;
    }

    @Transactional(value = "WMSTUDIOTransactionManager")
    @Override
    public void delete(Employee employee) {
        LOGGER.debug("Deleting Employee with {}", employee);
        this.wmGenericDao.delete(employee);
    }

    @Transactional(readOnly = true, value = "WMSTUDIOTransactionManager")
    @Override
    public Page<Employee> findAll(QueryFilter[] queryFilters, Pageable pageable) {
        LOGGER.debug("Finding all Employees");
        return this.wmGenericDao.search(queryFilters, pageable);
    }

    @Transactional(readOnly = true, value = "WMSTUDIOTransactionManager")
    @Override
    public Page<Employee> findAll(String query, Pageable pageable) {
        LOGGER.debug("Finding all Employees");
        return this.wmGenericDao.searchByQuery(query, pageable);
    }

    @Transactional(readOnly = true, value = "WMSTUDIOTransactionManager", timeout = 300)
    @Override
    public Downloadable export(ExportType exportType, String query, Pageable pageable) {
        LOGGER.debug("exporting data in the service WMSTUDIO for table Employee to {} format", exportType);
        return this.wmGenericDao.export(exportType, query, pageable);
    }

    @Transactional(readOnly = true, value = "WMSTUDIOTransactionManager", timeout = 300)
    @Override
    public void export(DataExportOptions options, Pageable pageable, OutputStream outputStream) {
        LOGGER.debug("exporting data in the service WMSTUDIO for table Employee to {} format", options.getExportType());
        this.wmGenericDao.export(options, pageable, outputStream);
    }

    @Transactional(readOnly = true, value = "WMSTUDIOTransactionManager")
    @Override
    public long count(String query) {
        return this.wmGenericDao.count(query);
    }

    @Transactional(readOnly = true, value = "WMSTUDIOTransactionManager")
    @Override
    public Page<Map<String, Object>> getAggregatedValues(AggregationInfo aggregationInfo, Pageable pageable) {
        return this.wmGenericDao.getAggregatedValues(aggregationInfo, pageable);
    }



}