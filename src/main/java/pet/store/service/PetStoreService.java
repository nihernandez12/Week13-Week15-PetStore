package pet.store.service;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import pet.store.controller.model.PetStoreData;
import pet.store.controller.model.PetStoreData.PetStoreCustomer;
import pet.store.controller.model.PetStoreData.PetStoreEmployee;
import pet.store.dao.PetStoreDao;
import pet.store.dao.EmployeeDao;
import pet.store.dao.CustomerDao;
import pet.store.entity.Customer;
import pet.store.entity.Employee;
import pet.store.entity.PetStore;

@Service
public class PetStoreService {

	@Autowired
	private PetStoreDao petStoreDao;
	
	@Autowired
	private EmployeeDao employeeDao;
	
	@Autowired
	private CustomerDao customerDao;

	@Transactional(readOnly = false)
	public PetStoreData savePetStore(PetStoreData petStoreData) {
		Long petStoreId = petStoreData.getPetStoreId();
		PetStore petStore = findOrCreatePetStore(petStoreId);
		
		copyPetStoreFields(petStore, petStoreData);
		return new PetStoreData(petStoreDao.save(petStore));
	}

	private void copyPetStoreFields(PetStore petStore, PetStoreData petStoreData) {
		petStore.setPetStoreName(petStoreData.getPetStoreName());
		petStore.setPetStoreAddress(petStoreData.getPetStoreAddress());
		petStore.setPetStoreCity(petStoreData.getPetStoreCity());
		petStore.setPetStoreState(petStoreData.getPetStoreState());
		petStore.setPetStoreZip(petStoreData.getPetStoreZip());
		petStore.setPetStorePhone(petStoreData.getPetStorePhone());
	}
	
	private PetStore findOrCreatePetStore(Long petStoreId) {
		PetStore petStore;
		
		if(Objects.isNull(petStoreId)) {
			petStore = new PetStore();
		} else {
			petStore = findPetStoreById(petStoreId);	
		}
		return petStore;
		}

	private PetStore findPetStoreById(Long petStoreId) {
		return petStoreDao.findById(petStoreId)
					.orElseThrow(() -> new NoSuchElementException("Pet store with ID=" + petStoreId + " was not found."));
	}

	@Transactional(readOnly = false)
	public PetStoreEmployee saveEmployee(Long petStoreId, PetStoreEmployee petStoreEmployee) {
		PetStore petStore = findPetStoreById(petStoreId);
		
		Long employeeId = petStoreEmployee.getEmployeeId();
		Employee employee = findOrCreatePetStoreEmployee(employeeId, petStoreId);
		copyEmployeeFields(employee, petStoreEmployee);
		employee.setPetStore(petStore);
		petStore.getEmployees().add(employee);
		
		return new PetStoreEmployee(employeeDao.save(employee));
	}

	private void copyEmployeeFields(Employee employee, PetStoreEmployee petStoreEmployee) {
		employee.setEmployeeId(petStoreEmployee.getEmployeeId());
		employee.setEmployeeFirstName(petStoreEmployee.getEmployeeFirstName());
		employee.setEmployeeLastName(petStoreEmployee.getEmployeeLastName());
		employee.setEmployeePhone(petStoreEmployee.getEmployeePhone());
		employee.setEmployeeJobTitle(petStoreEmployee.getEmployeeJobTitle());
	}
	
		private Employee findEmployeeById(Long petStoreId, Long employeeId) {
			Employee employee = employeeDao.findById(employeeId).orElseThrow(
					() -> new NoSuchElementException("Employee with ID=" + employeeId + " does not exist."));
		
		if(employee.getPetStore().getPetStoreId() != petStoreId) {
			throw new IllegalArgumentException(
					"Employee with ID-" + employeeId + " is not an employee at pet store with ID=" + petStoreId + ".");
		}
			return employee;
		}

	private Employee findOrCreatePetStoreEmployee(Long employeeId, Long petStoreId) {
		Employee employee;
		
		if(Objects.isNull(employeeId)) {
			employee = new Employee();
		} else {
			employee = findEmployeeById(employeeId, petStoreId);
		}
		return employee;
	}

	@Transactional(readOnly = false)
	public PetStoreCustomer saveCustomer(Long petStoreId, PetStoreCustomer petStoreCustomer) {
		PetStore petStore = findPetStoreById(petStoreId);
		Long customerId = petStoreCustomer.getCustomerId();
		Customer customer = findOrCreatePetStoreCustomer(customerId, petStoreId);
		copyCustomerFields(customer, petStoreCustomer);
		customer.getPetStores().add(petStore);
		petStore.getCustomers().add(customer);
		return new PetStoreCustomer(customerDao.save(customer)) ;
	}
	
	private Customer findOrCreatePetStoreCustomer(Long customerId, Long petStoreId) {
		Customer customer;
		
		if (Objects.isNull(customerId)) {
			customer = new Customer();
		} else {
			customer = findCustomerById(customerId, petStoreId);
		}
		return customer;
	}

	private void copyCustomerFields(Customer customer, PetStoreCustomer petStoreCustomer) {
	customer.setCustomerId(petStoreCustomer.getCustomerId());
	customer.setCustomerFirstName(petStoreCustomer.getCustomerFirstName());
	customer.setCustomerLastName(petStoreCustomer.getCustomerLastName());
	customer.setCustomerEmail(petStoreCustomer.getCustomerEmail());
		
	}
	
	private Customer findCustomerById(Long customerId, Long petStoreId) {
		Customer customer = customerDao.findById(customerId).orElseThrow(
				() -> new NoSuchElementException("Customer with ID=" + customerId + " was not found."));
		boolean found = false;
		for(PetStore petStore: customer.getPetStores())  {
			if(petStore.getPetStoreId() == petStoreId) {
				found = true;
				break;
			} 
		}
		if(!found) {
			throw new IllegalArgumentException("Customer with ID=" + customerId + " does not match the pet store ID " + petStoreId);

	}
	return customer;
	}
	
	@Transactional(readOnly = true)
	public List<PetStoreData> retrieveAllPetStores() {
		List<PetStore> petStores = petStoreDao.findAll();
		List<PetStoreData> response = new LinkedList<>();
		
		for(PetStore petStore : petStores) {
			PetStoreData psd = new PetStoreData(petStore);
			psd.getCustomers().clear();
			psd.getEmployees().clear();
			
			response.add(psd);
		}
		return response;
	}

	@Transactional(readOnly = true)
	public PetStoreData retrievePetStoreById(Long petStoreId) {
		PetStore petStore = petStoreDao.findById(petStoreId).orElseThrow(() -> new NoSuchElementException("Pet store with ID=" + petStoreId + " was not found."));
		boolean found = false;
			if(petStore.getPetStoreId() == petStoreId) {
				found = true;
			} 
		if(!found) {
			throw new IllegalArgumentException("Customer with ID=" + petStoreId + " does not match the pet store ID " + petStoreId);
		}
	return new PetStoreData(petStore);
				
	}

	@Transactional(readOnly = false)
	public void deletePetStoreById(Long petStoreId) {
		PetStore petStore = findPetStoreById(petStoreId);
		petStoreDao.delete(petStore);
		
	}
}

	

