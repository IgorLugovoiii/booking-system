package com.example.inventory_service.controllers;

import com.example.inventory_service.dtos.ItemRequest;
import com.example.inventory_service.dtos.ItemResponse;
import com.example.inventory_service.dtos.ItemSearchParams;
import com.example.inventory_service.services.api.ItemService;
import com.example.inventory_service.services.impl.ItemServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
@Tag(name = "Item controller", description = "Controller for managing items")
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    @Operation(summary = "Returns all items")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "returns all items",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ItemResponse.class))
                    )),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Items not found")
    })
    public ResponseEntity<List<ItemResponse>> findAllItems(){
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    @Operation(summary = "Returns items by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item found"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponse> findItemById(
            @Parameter(description = "ID of the item to find", example = "1")
            @PathVariable Long itemId) throws JsonProcessingException {
        return new ResponseEntity<>(itemService.findById(itemId),HttpStatus.OK);
    }

    @Operation(summary = "Creates item")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item created"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<ItemResponse> createItem(@RequestBody @Valid ItemRequest itemRequest) throws JsonProcessingException {
        return new ResponseEntity<>(itemService.createItem(itemRequest),HttpStatus.CREATED);
    }

    @Operation(summary = "Update item")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @PutMapping("/{itemId}")
    public ResponseEntity<ItemResponse> updateItem(
            @Parameter(description = "ID of the item to update", example = "1")
            @PathVariable @Valid Long itemId, @RequestBody ItemRequest itemRequest) throws JsonProcessingException {
        return new ResponseEntity<>(itemService.updateItem(itemId,itemRequest),HttpStatus.OK);
    }

    @Operation(summary = "Delete item")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItemById(
            @Parameter(description = "ID of the item to delete", example = "1")
            @PathVariable Long itemId) throws JsonProcessingException {
        itemService.deleteById(itemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Searching items with different params")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item found successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ItemResponse>> searchItems(
            ItemSearchParams itemSearchParams,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<ItemResponse> itemResponses = itemService.searchItem(itemSearchParams, PageRequest.of(page,size));
        return new ResponseEntity<>(itemResponses, HttpStatus.OK);
    }
}
