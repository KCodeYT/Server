package cn.nukkit.level.generator.standard.biome.map;

import cn.nukkit.Nukkit;
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
final class BiomeMapDeserializer extends JsonDeserializer<BiomeMap> {
    @Override
    public BiomeMap deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String nextName = p.nextFieldName();
        Preconditions.checkState("id".equals(nextName), "first field must be \"id\", not \"%s\"", nextName);
        Identifier id = Identifier.fromString(p.nextTextValue());
        p.nextToken();

        return Nukkit.YAML_MAPPER.readValue(p, StandardGeneratorRegistries.biomeMap().get(id));
    }
}
