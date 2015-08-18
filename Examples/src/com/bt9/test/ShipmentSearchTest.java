/*
 * ReportSchedulerTaskTest.java        17 Jun 2015
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.affinity.AffinityKey;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.AtomicConfiguration;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.IgniteSpiException;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.joda.time.DateTime;

/**
 * Test of Ignite search capability
 * 
 */
public class ShipmentSearchTest {

	private static final String SHIPMENT_CACHE = "ShipmentCache";
	private static final String SHIPMENT_SEGMENT_CACHE = "ShipmentSegmentCache";
	
	// Ignite instance
	private static Ignite ignite;
	
	public static void main(String[] args) {
		
		ShipmentSearchTest test = new ShipmentSearchTest();
		
		test.startIgnite();
		
		test.loadCache();
		
		test.testQueries();
		
		test.stopIgnite();
	}
		

	private void startIgnite() {
		// Config
		IgniteConfiguration igniteConfig = new IgniteConfiguration();

		// Set Ignite Configuration
		igniteConfig.setIgniteHome(System.getProperty("user.dir"));
		igniteConfig.setLocalHost("127.0.0.1");
		igniteConfig.setGridName("X2S_Cluster");
		igniteConfig.setGridLogger(new Slf4jLogger());
		
		// Set Discovery SPI
		TcpDiscoverySpi discoSpi = new TcpDiscoverySpi();
		discoSpi.setLocalAddress("127.0.0.1");
		// Set ip addresses
		TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
		List<String> gridNodes = new ArrayList<>();
		gridNodes.add("127.0.0.1");				
		try {
			ipFinder.setAddresses(gridNodes);
		} catch (IgniteSpiException e) {
			e.printStackTrace();
		}
		discoSpi.setIpFinder(ipFinder);
		igniteConfig.setDiscoverySpi(discoSpi);

		//  Set Cache configurations
		CacheConfiguration<?, ?> shipmentCacheConfig = new CacheConfiguration<>();
		shipmentCacheConfig.setName(SHIPMENT_CACHE);
		shipmentCacheConfig.setCacheMode(CacheMode.PARTITIONED);
		shipmentCacheConfig.setAtomicityMode(CacheAtomicityMode.ATOMIC);
		shipmentCacheConfig.setIndexedTypes(UUID.class, CachedShipment.class);
		CacheConfiguration<?, ?> shipmentSegmentCacheConfig = new CacheConfiguration<>();
		shipmentSegmentCacheConfig.setName(SHIPMENT_SEGMENT_CACHE);
		shipmentSegmentCacheConfig.setCacheMode(CacheMode.PARTITIONED);
		shipmentSegmentCacheConfig.setAtomicityMode(CacheAtomicityMode.ATOMIC);
		shipmentSegmentCacheConfig.setIndexedTypes(AffinityKey.class, CachedShipmentSegment.class);
		
		igniteConfig.setCacheConfiguration(shipmentCacheConfig, shipmentSegmentCacheConfig);
		
		// Define atomic configurations
		AtomicConfiguration atomicCfg = new AtomicConfiguration();
		atomicCfg.setCacheMode(CacheMode.REPLICATED);
		igniteConfig.setAtomicConfiguration(atomicCfg);

		// Start Ignite
		ignite = Ignition.start(igniteConfig);
	}

	private void stopIgnite() {
		// Stop Ignite
		Ignition.stop("TestIgnite",true);
	}

	private void testQueries() {
		
		// Not working query
		query_1();

		// Working query
		query_2();

		// Working query
		query_3();
	}
	
	private void loadCache() {
		System.out.println("\n================== " + "loadCache()");
		
		// load shipments to cache
		int nShipments = 5;
		IgniteCache<UUID, CachedShipment> shipmentCache = ignite.cache(SHIPMENT_CACHE);
		IgniteCache<AffinityKey<UUID>, CachedShipmentSegment> shipmentSegmentCache = ignite.cache(SHIPMENT_SEGMENT_CACHE);
		
		long currTime = System.currentTimeMillis();
		long yearDuration = 365l * 24 * 60 * 60 * 1000; // in ms
		for (long i = 0; i < nShipments; i++) {
			String name = "SH_" + i;
			String descr = name + "_DESCR";
			String startSiteName = (i < 2) ? "SITE_1" : "SITE_2";
			ShipmentStatus status = (i < 3) ? ShipmentStatus.OPENED : ShipmentStatus.CLOSED;
			Date startDate = new Date(currTime - i * yearDuration);
			String destOrgName = (i < 4) ? "COMP_1" : "COMP_2";
			CarrierType carrierType = (i < 2) ? CarrierType.AVIA : CarrierType.SHIP;

			CachedShipment cachedShipment = new CachedShipment(name, descr, startSiteName, startDate, status, destOrgName, carrierType);
			shipmentCache.put(cachedShipment.getId(), cachedShipment);
			System.out.println(cachedShipment);
			
			// load segments
			int nSegments = (i < 3) ? 2 : 3;
			for (int j = 0; j < nSegments; j++) {
				String endSiteName = "SITE_3";

				CachedShipmentSegment cachedShipmentSegment = new CachedShipmentSegment(cachedShipment.getId(), startSiteName, endSiteName);
				shipmentSegmentCache.put(cachedShipmentSegment.key(), cachedShipmentSegment);
				System.out.println("\t" + cachedShipment);
			}
		}
	}


	private void query_1() {
		System.out.println("\n================== " + "query_1()");
		IgniteCache<UUID, CachedShipment> shipmentCache = ignite.cache(SHIPMENT_CACHE);
        try {
			String queryStmt =
				"select count(*) " +
				"from CachedShipment " +
				"where status = ? and startDate > ?";

			Date startDate = DateTime.parse("2010-01-01").toDate();
			
			// Execution plan
			String explainStmt = "explain analyze " + queryStmt;
			QueryCursor<List<?>> planResults = shipmentCache.query(new SqlFieldsQuery(explainStmt).setArgs(ShipmentStatus.OPENED, startDate));
			System.out.println("Execution Plan:");
			for (List<?> entry : planResults) {
				System.out.println("\n" + entry);
			}

			// Retrieve data
    		long start = System.currentTimeMillis();
			QueryCursor<List<?>> results = shipmentCache.query(new SqlFieldsQuery(queryStmt).setArgs(ShipmentStatus.OPENED, startDate));

			System.out.println("\ncount(*) " + results.getAll().get(0).get(0));
			long end = System.currentTimeMillis();
			System.out.println("Execution time: " + (end-start) + "msec");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void query_2() {
		System.out.println("\n================== " + "query_2()");
		IgniteCache<UUID, CachedShipment> shipmentCache = ignite.cache(SHIPMENT_CACHE);
        try {
			String queryStmt =
				"select count(*) " +
				"from CachedShipment " +
				"where status = ? and startDate < ?";

			Date startDate = DateTime.parse("2016-01-01").toDate();
			
			// Execution plan
			String explainStmt = "explain analyze " + queryStmt;
			QueryCursor<List<?>> planResults = shipmentCache.query(new SqlFieldsQuery(explainStmt).setArgs(ShipmentStatus.OPENED, startDate));
			System.out.println("Execution Plan:");
			for (List<?> entry : planResults) {
				System.out.println("\n" + entry);
			}

			// Retrieve data
    		long start = System.currentTimeMillis();
			QueryCursor<List<?>> results = shipmentCache.query(new SqlFieldsQuery(queryStmt).setArgs(ShipmentStatus.OPENED, startDate));

			System.out.println("\ncount(*) " + results.getAll().get(0).get(0));
			long end = System.currentTimeMillis();
			System.out.println("Execution time: " + (end-start) + "msec");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void query_3() {
		System.out.println("\n================== " + "query_3()");
		IgniteCache<UUID, CachedShipment> shipmentCache = ignite.cache(SHIPMENT_CACHE);
        try {
			String queryStmt =
				"select count(*) " +
				"from CachedShipment " +
				"where status = ? and startDate < ? and startDate > ?";

			Date startDate = DateTime.parse("2016-01-01").toDate();
			Date startDate2 = DateTime.parse("2010-01-01").toDate();
			
			// Execution plan
			String explainStmt = "explain analyze " + queryStmt;
			QueryCursor<List<?>> planResults = shipmentCache.query(new SqlFieldsQuery(explainStmt).setArgs(ShipmentStatus.OPENED, startDate, startDate2));
			System.out.println("Execution Plan:");
			for (List<?> entry : planResults) {
				System.out.println("\n" + entry);
			}

			// Retrieve data
    		long start = System.currentTimeMillis();
			QueryCursor<List<?>> results = shipmentCache.query(new SqlFieldsQuery(queryStmt).setArgs(ShipmentStatus.OPENED, startDate, startDate2));

			System.out.println("\ncount(*) " + results.getAll().get(0).get(0));
			long end = System.currentTimeMillis();
			System.out.println("Execution time: " + (end-start) + "msec");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
