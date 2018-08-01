package io.bonitoo.flux.operators;

import java.util.HashMap;

import io.bonitoo.flux.Flux;
import io.bonitoo.flux.FluxChain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (17/07/2018 12:15)
 */
@RunWith(JUnitPlatform.class)
class DistinctFluxTest {

    @Test
    void distinct() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy("_measurement")
                .distinct("_measurement");

        String expected = "from(db:\"telegraf\") |> group(by: [\"_measurement\"]) |> distinct(column: \"_measurement\")";
        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void distinctByParameter() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy("_measurement")
                .distinct()
                .withPropertyNamed("column");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("column", "\"_value\"");

        String expected = "from(db:\"telegraf\") |> group(by: [\"_measurement\"]) |> distinct(column: \"_value\")";
        Assertions.assertThat(flux.print(new FluxChain().addParameters(parameters))).isEqualToIgnoringWhitespace(expected);
    }
}