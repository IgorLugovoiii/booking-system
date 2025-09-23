package com.example.inventory_service.mappers;

import com.example.inventory_service.kafka.ItemEvent;
import com.example.inventory_service.models.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemEventMapper {
    @Mapping(target = "eventType", constant = "item.created")
    ItemEvent toCreatedEvent(Item item);
    @Mapping(target = "eventType", constant = "item.updated")
    ItemEvent toUpdatedEvent(Item item);
    @Mapping(target = "eventType", constant = "item.deleted")
    ItemEvent toDeletedEvent(Item item);
}
