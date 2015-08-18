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
import java.util.UUID;

import org.apache.ignite.cache.affinity.AffinityKey;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

/**
 * @author sasha
 *
 */
public class CachedShipmentSegment implements Externalizable {

	private UUID id;
	
	@QuerySqlField(index=true, orderedGroups={@QuerySqlField.Group(name="group_segment", order=0)})
	private UUID shipmentId;
	
	@QuerySqlField(index=true, orderedGroups={@QuerySqlField.Group(name="group_segment", order=1)})
	private String startSiteName;
	
	@QuerySqlField(index = true)
	private String endSiteName;
    
	/** Custom cache key to guarantee that segment is always collocated with its shipment. */
    private transient AffinityKey<UUID> key;
    
	/**
	 * Default constructor
	 */
	public CachedShipmentSegment() {
	}

	/**
	 * Copy constructor
	 */
	public CachedShipmentSegment(UUID shipmentId, String startSiteName, String endSiteName) {
		this.id = UUID.randomUUID();
		this.shipmentId = shipmentId;
		this.startSiteName = startSiteName;
		this.endSiteName = endSiteName;
	}
	
    /**
     * Gets cache affinity key. Since segment needs to be collocated with shipment, we create
     * custom affinity key to guarantee this collocation.
     *
     * @return Custom affinity key
     */
    public AffinityKey<UUID> key() {
        if (key == null)
            key = new AffinityKey<>(id, shipmentId);

        return key;
    }
    
	//================= Setters & Getters

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getShipmentId() {
		return shipmentId;
	}

	public void setShipmentId(UUID shipmentId) {
		this.shipmentId = shipmentId;
	}

	public String getStartSiteName() {
		return startSiteName;
	}

	public void setStartSiteName(String startSiteName) {
		this.startSiteName = startSiteName;
	}

	public String getEndSiteName() {
		return endSiteName;
	}

	public void setEndSiteName(String endSiteName) {
		this.endSiteName = endSiteName;
	}
	
	public String toString() {
		return "{shipmentId:" + shipmentId + ",startSiteName:" + startSiteName + ",endSiteName:" + endSiteName + "}";
	}

	// =======================Externalizable Method
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		// Read variables from object input
		id = (UUID) in.readObject();
		shipmentId = (UUID) in.readObject();
		startSiteName = (String) in.readObject();
		endSiteName = (String) in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// Write out variables into output object
		out.writeObject(id);
		out.writeObject(shipmentId);
		out.writeObject(startSiteName);
		out.writeObject(endSiteName);
	}

}
