/*
 * The MIT License
 * Copyright © 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.bonitoo.flux;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.bonitoo.AbstractTest;
import io.bonitoo.flux.impl.FluxClientImpl;
import io.bonitoo.flux.option.FluxConnectionOptions;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 09:35)
 */
public abstract class AbstractITFluxClient extends AbstractTest {

    private static final Logger LOG = Logger.getLogger(AbstractITFluxClient.class.getName());

    static final String DATABASE_NAME = "flux_database";

    FluxClient fluxClient;
    InfluxDB influxDB;

    @BeforeEach
    protected void setUp() {

        String fluxIP = System.getenv().getOrDefault("FLUX_IP", "127.0.0.1");
        String fluxPort = System.getenv().getOrDefault("FLUX_PORT_API", "8093");
        String fluxURL = "http://" + fluxIP + ":" + fluxPort;
        LOG.log(Level.FINEST, "Flux URL: {0}", fluxURL);

        FluxConnectionOptions fluxConnectionOptions = FluxConnectionOptions.builder()
                .url(fluxURL)
                .orgID("00")
                .build();

        fluxClient = new FluxClientImpl(fluxConnectionOptions);
        fluxClient.disableGzip();

        String influxdbIP = System.getenv().getOrDefault("INFLUXDB_IP", "127.0.0.1");
        String influxdbPort = System.getenv().getOrDefault("INFLUXDB_PORT_API", "8086");
        String influxURL = "http://" + influxdbIP + ":" + influxdbPort;
        LOG.log(Level.FINEST, "Influx URL: {0}", influxURL);

        influxDB = InfluxDBFactory.connect(influxURL);
        influxDB.setDatabase(DATABASE_NAME);
        influxDB.query(new Query("CREATE DATABASE " + DATABASE_NAME, DATABASE_NAME));
    }

    @AfterEach
    protected void after() {

        influxDB.query(new Query("DROP DATABASE " + DATABASE_NAME, DATABASE_NAME));

        fluxClient.close();
        fluxClient.close();
    }
}