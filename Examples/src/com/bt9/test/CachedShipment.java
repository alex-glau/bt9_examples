/*
 * Tag.java        21 Oct 2014
 *
 * Copyright (c) 2012-2014 BT9 Ltd.
 * 33 Dolev St. | P.O.B 54, Migdal Tefen, 2495900, Israel.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * BT9 Ltd. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered 
 * into with BT9.
 */
package com.bt9.test;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;
import java.util.UUID;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

/**
 * @author sasha
 *
 */
public class CachedShipment implements Externalizable {

	@QuerySqlField(index = true)
	private UUID id;
	
	@QuerySqlField(index = true)
	private String name;
	
	@QuerySqlField
	private String description;
	
	@QuerySqlField(index=true)
//	@QuerySqlField(index=true, orderedGroups={@QuerySqlField.Group(name="group_shipment", order=1)})
	private String startSiteName;
	
	@QuerySqlField(index=true, orderedGroups={@QuerySqlField.Group(name="group_shipment", order=1)})
	private Date startDate;
	
	@QuerySqlField(index=true, orderedGroups={@QuerySqlField.Group(name="group_shipment", order=0)})
	private ShipmentStatus status;
	
	@QuerySqlField(index = true)
	private String destOrgName;
	
	@QuerySqlField(index = true)
	private CarrierType carrierType;

	/**
	 * Default constructor
	 */
	public CachedShipment() {
	}

	/**
	 * Copy constructor
	 */
	public CachedShipment(String name, String description, String startSiteName, Date startDate, ShipmentStatus status, String destOrgName, CarrierType carrierType) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.description = description;
		this.startSiteName = startSiteName;
		this.startDate = startDate;
		this.status = status;
		this.destOrgName = destOrgName;
		this.carrierType = carrierType;
	}
	
	//================= Setters & Getters
	
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStartSiteName() {
		return startSiteName;
	}

	public void setStartSiteName(String startSiteName) {
		this.startSiteName = startSiteName;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public ShipmentStatus getStatus() {
		return status;
	}

	public void setStatus(ShipmentStatus status) {
		this.status = status;
	}

	public String getDestOrgName() {
		return destOrgName;
	}

	public void setDestOrgName(String destOrgName) {
		this.destOrgName = destOrgName;
	}

	public CarrierType getCarrierType() {
		return carrierType;
	}

	public void setCarrierType(CarrierType carrierType) {
		this.carrierType = carrierType;
	}
	
	public String toString() {
		return "{id:" + id + ",name:" + name + ",description:" + description + ",startSiteName:" + startSiteName + 
						",startDate:" + startDate + ",status:" + status + ",destOrgName:" + destOrgName + ",carrierType:" + carrierType + "}";
	}

	// =======================Externalizable Method
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		// Read variables from object input
		id = (UUID) in.readObject();
		name = (String) in.readObject();
		description = (String) in.readObject();
		startSiteName = (String) in.readObject();
		startDate = (Date) in.readObject();
		status = (ShipmentStatus) in.readObject();
		destOrgName = (String) in.readObject();
		carrierType = (CarrierType) in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// Write out variables into output object
		out.writeObject(id);
		out.writeObject(name);
		out.writeObject(description);
		out.writeObject(startSiteName);
		out.writeObject(startDate);
		out.writeObject(status);
		out.writeObject(destOrgName);
		out.writeObject(carrierType);
	}

}
