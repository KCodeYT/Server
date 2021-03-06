package cn.nukkit.level.generator.standard.population;

import cn.nukkit.Nukkit;
import cn.nukkit.level.generator.standard.misc.NextGenerationPass;
import cn.nukkit.level.generator.standard.registry.StandardGeneratorRegistries;
import cn.nukkit.utils.Identifier;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
final class PopulatorDeserializer extends JsonDeserializer<Populator> {
    @Override
    public Populator deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String nextName = p.nextFieldName();
        Preconditions.checkState("id".equals(nextName), "first field must be \"id\", not \"%s\"", nextName);
        Identifier id = Identifier.fromString(p.nextTextValue());
        p.nextToken();

        return id == NextGenerationPass.ID
               ? NextGenerationPass.INSTANCE : Nukkit.YAML_MAPPER.readValue(p, StandardGeneratorRegistries.populator().get(id));
    }
}
