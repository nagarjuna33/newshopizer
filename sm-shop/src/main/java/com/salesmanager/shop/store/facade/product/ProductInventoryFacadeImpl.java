package com.salesmanager.shop.store.facade.product;

import static com.salesmanager.shop.util.ReadableEntityUtil.createReadableList;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.catalog.product.availability.ProductAvailabilityService;
import com.salesmanager.core.business.services.catalog.product.instance.ProductInstanceService;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.availability.ProductAvailability;
import com.salesmanager.core.model.catalog.product.instance.ProductInstance;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.mapper.inventory.PersistableInventoryMapper;
import com.salesmanager.shop.mapper.inventory.ReadableInventoryMapper;
import com.salesmanager.shop.model.catalog.product.inventory.PersistableInventory;
import com.salesmanager.shop.model.catalog.product.inventory.ReadableInventory;
import com.salesmanager.shop.model.entity.ReadableEntityList;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.store.controller.product.facade.ProductInventoryFacade;

@Service("productInventoryFacade")
@Profile({ "default", "cloud", "gcp", "aws", "mysql", "local" })
public class ProductInventoryFacadeImpl implements ProductInventoryFacade {

	@Autowired
	private ProductAvailabilityService productAvailabilityService;

	@Autowired
	private ProductService productService;
	
	@Autowired
	private ProductInstanceService productInstanceService;

	@Autowired
	private MerchantStoreService merchantStoreService;

	@Autowired
	private ReadableInventoryMapper readableInventoryMapper;

	@Autowired
	private PersistableInventoryMapper productInventoryMapper;

	/**
	@Override
	public ReadableEntityList<ReadableInventory> getInventory(Long productId, MerchantStore store, String child,
			Language language, int page, int count) {
		Product product = getProductById(productId);
		validateProductHasSameStore(store, product);

		Page<ProductAvailability> availabilities = productAvailabilityService.listByProduct(product, store, child, page,
				count);

		List<ReadableInventory> inventories = availabilities.stream()
				.map(pa -> readableInventoryMapper.convert(pa, store, language)).collect(Collectors.toList());
		return createReadableList(availabilities, inventories);
	}
	**/

	private void validateProductHasSameStore(MerchantStore store, Product product) {
		if (!product.getMerchantStore().getId().equals(store.getId())) {
			throw new ResourceNotFoundException("Product with id [" + product.getId() + "] not found for store id ["
					+ store.getInvoiceTemplate() + "]");
		}
	}

	@Override
	public void delete(Long inventoryId, MerchantStore store) {
		Optional<ProductAvailability> availability = productAvailabilityService.getById(inventoryId, store);
		try {
			if (availability.isPresent()) {
				productAvailabilityService.delete(availability.get());
			}
		} catch (ServiceException e) {
			throw new ServiceRuntimeException("Error while deleting inventory", e);
		}
	}

	/*
	 * @Override public ReadableInventory get(Long inventoryId, MerchantStore store,
	 * Language language) { ProductAvailability availability =
	 * getAvailabilityById(store, inventoryId); return
	 * readableInventoryMapper.convert(availability, store, language); }
	 */


	//public ReadableInventory get(String child, Language language) {
		/*
		 * Product product = getProductById(productId); MerchantStore store =
		 * getMerchantStore(child);
		 * 
		 * if (isStoreParentNotExist(store) ||
		 * store.getParent().getId().equals(product.getMerchantStore().getId())) { throw
		 * new ResourceNotFoundException( "MerchantStore [" + child +
		 * "] is not a store of retailer [" + store.getCode() + "]"); }
		 * 
		 * ProductAvailability availability =
		 * productAvailabilityService.getByStore(product, store) .orElseThrow(() -> new
		 * ResourceNotFoundException("Inventory with not found"));
		 * 
		 * return this.readableInventory(availability, store, language);
		 */
	//}

	/**
	private boolean isStoreParentNotExist(MerchantStore store) {
		return Objects.isNull(store.getParent());
	}
	

	private MerchantStore getMerchantStore(String child) {
		try {
			return Optional.ofNullable(merchantStoreService.getByCode(child))
					.orElseThrow(() -> new ResourceNotFoundException("MerchantStore [" + child + "] not found"));
		} catch (ServiceException e) {
			throw new ServiceRuntimeException("Error while getting inventory", e);
		}
	}
	
	private ReadableInventory readableInventory(ProductAvailability availability, MerchantStore store, Language language) {
		return readableInventoryMapper.convert(availability, store, language);
	}
	**/

	
	private Product getProductById(Long productId, MerchantStore store) {
		return productService
				.retrieveById(productId, store)
				.orElseThrow(() -> new ResourceNotFoundException("Product with id [" + productId + "] not found"));
	}
	
	private ProductInstance getProductByInstance(Long instanceId, MerchantStore store) {
		return productInstanceService.getById(instanceId, store).orElseThrow(() -> new ResourceNotFoundException("Product with instance [" + instanceId + "] not found"));

	}

	@Override
	public ReadableInventory add(PersistableInventory inventory, MerchantStore store,
			Language language) {
		Validate.notNull(store, "MerchantStore cannot be null");
		ProductAvailability availability = getProductAvailabilityToSave(inventory, store);
		saveOrUpdate(availability);
		return get(availability.getId(), store, language);
	}

	private void saveOrUpdate(ProductAvailability availability) {
		try {
			productAvailabilityService.saveOrUpdate(availability);
		} catch (ServiceException e) {
			throw new ServiceRuntimeException("Cannot create Inventory", e);
		}
	}

	private ProductAvailability getProductAvailabilityToSave(PersistableInventory inventory, MerchantStore store) {
		
		return productInventoryMapper.convert(inventory, store, store.getDefaultLanguage());

	}

	@Override
	public ReadableInventory get(Long inventoryId, MerchantStore store, Language language) {


		ProductAvailability availability = productAvailabilityService.getById(inventoryId, store)
				.orElseThrow(() -> new ResourceNotFoundException("Inventory with id [" + inventoryId + "] not found"));
		return readableInventoryMapper.convert(availability, store, language);
	}

	@Override
	public void update(PersistableInventory inventory, MerchantStore store, Language language) {

		Validate.notNull(inventory, "Inventory cannot be null");
		Validate.notNull(store, "MerchantStore cannot be null");

		Set<ProductAvailability> originAvailability = null;
		Product product = null;
		
		if(inventory.getProductId()!=null && inventory.getProductId().longValue()>0) {
			product = this.getProductById(inventory.getProductId(), store);
			originAvailability = product.getAvailabilities();
		} else {
			if(inventory.getInstance() != null && inventory.getId().longValue() > 0) {
				ProductInstance instance = this.getProductByInstance(inventory.getInstance(), store);
				originAvailability = instance.getAvailabilities();
				product = instance.getProduct();
			}
		}


		ProductAvailability availability = getAvailabilityById(store, inventory.getId());
		
		if (availability == null) {
			throw new ResourceNotFoundException(
					"Availability with id [" + inventory.getId() + "] not found");
		}
		
		if(CollectionUtils.isEmpty(originAvailability)) {
			throw new ResourceNotFoundException(
					"No availability with id [" + inventory.getId() + "] found");
		}

		
		ProductAvailability avail = originAvailability.stream()
		  .filter(a -> a.getId().longValue() == inventory.getId())
		  .findAny()
		  .orElse(null);
		
		if (avail == null) {
			throw new ResourceNotFoundException(
					"Availability with id [" + inventory.getId() + "] not found");
		}


		inventory.setProductId(product.getId());

		availability = productInventoryMapper.merge(inventory, availability, store, language);
		availability.setProduct(product);
		availability.setMerchantStore(store);
		saveOrUpdate(availability);
	}

	private ProductAvailability getAvailabilityById(MerchantStore store, Long id) {
		return productAvailabilityService.getById(id, store)
				.orElseThrow(() -> new ResourceNotFoundException("Inventory with id [" + id + "] not found"));
	}


	@Override
	public ReadableEntityList<ReadableInventory> get(String sku, MerchantStore store, Language language, int page, int count) {
		Validate.notNull(sku, "Product sku cannot be null");
		Validate.notNull(store, "MerchantStore code cannot be null");
		Validate.notNull(language, "Language cannot be null");
		
		Page<ProductAvailability> availabilities = productAvailabilityService.getBySku(sku, page, count);
		
		List<ReadableInventory> returnList = availabilities.getContent().stream().map(i -> this.readableInventoryMapper.convert(i, store, language))
				.collect(Collectors.toList());

		return createReadableList(availabilities, returnList);
	}



}
