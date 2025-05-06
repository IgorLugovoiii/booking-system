package com.example.inventory_service.controllers;

import com.example.inventory_service.dtos.ItemRequest;
import com.example.inventory_service.dtos.ItemResponse;
import com.example.inventory_service.services.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@Tag(name = "Item controller", description = "Controller for managing items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService){
        this.itemService = itemService;
    }

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
    public ResponseEntity<ItemResponse> findItemById(@PathVariable Long itemId){
        return new ResponseEntity<>(itemService.findById(itemId),HttpStatus.OK);
    }

    @Operation(summary = "Creates item")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item created"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<ItemResponse> createItem(@RequestBody @Valid ItemRequest itemRequest){
        return new ResponseEntity<>(itemService.createItem(itemRequest),HttpStatus.CREATED);
    }

    @Operation(summary = "Update item")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @PutMapping("/{itemId}")
    public ResponseEntity<ItemResponse> updateItem(@PathVariable @Valid Long itemId, @RequestBody ItemRequest itemRequest){
        return new ResponseEntity<>(itemService.updateItem(itemId,itemRequest),HttpStatus.OK);
    }

    @Operation(summary = "Delete item")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItemById(@PathVariable Long itemId){
        itemService.deleteById(itemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
