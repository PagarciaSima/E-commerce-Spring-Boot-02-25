package spring.ecommerce.controller;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.entity.ImageEntity;
import spring.ecommerce.service.ImageService;

/**
 * Controller for managing image operations.
 */

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/api/v1/images")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Images", description = "API for managing images")
@SecurityRequirement(name = "bearerAuth")  

public class ImageController {

    private final ImageService imageService;

    /**
     * Uploads an image file and saves it.
     * 
     * @param file the image file to be uploaded
     * @return a ResponseEntity with the created image or an error response
     */
    @Operation(
        summary = "Upload an image",
        description = "Uploads an image file and saves it to the database.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Image successfully uploaded", content = @Content(schema = @Schema(implementation = ImageEntity.class))),
            @ApiResponse(responseCode = "400", description = "Invalid image file", content = @Content)
        }
    )
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        ImageEntity savedImage = this.imageService.saveImage(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
    }

    /**
     * Retrieves an image by its ID and returns it as a Base64 encoded string.
     * <p>
     * This method fetches the image from the database using the provided ID and converts the image's byte array 
     * into a Base64 encoded string, which can be used to display the image in web applications, for example.
     * If the image is not found, it returns a NOT_FOUND response.
     * </p>
     *
     * @param id the ID of the image to retrieve
     * @return a ResponseEntity containing the Base64 encoded image string if found, or a NOT_FOUND response if the image does not exist
     */
    @Operation(
        summary = "Retrieve an image by ID (Base64)",
        description = "Fetches an image from the database and returns it as a Base64-encoded string.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Image found", content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "Image not found", content = @Content)
        }
    )
    @GetMapping("/image/{id}")
    public ResponseEntity<String> getImageAsBase64(@PathVariable Long id) {
        Optional<ImageEntity> image = this.imageService.getImageById(id);
        if (image.isPresent()) {
            String base64Image = Base64.getEncoder().encodeToString(image.get().getPicByte());
            return ResponseEntity.ok(base64Image);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    /**
     * Retrieves an image by its name.
     * 
     * @param name the name of the image to retrieve
     * @return a ResponseEntity with the image or a NOT_FOUND response
     */
    @Operation(
        summary = "Retrieve an image by name",
        description = "Fetches an image from the database by its name.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Image found", content = @Content(schema = @Schema(implementation = ImageEntity.class))),
            @ApiResponse(responseCode = "404", description = "Image not found", content = @Content)
        }
    )
    @GetMapping("/name/{name}")
    public ResponseEntity<?> getImageByName(@PathVariable String name) {
        Optional<ImageEntity> image = this.imageService.getImageByName(name);
        return image.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    /**
     * Deletes an image by its ID.
     * 
     * @param id the ID of the image to delete
     * @return a ResponseEntity with NO_CONTENT status or an error response
     */
    @Operation(
        summary = "Delete an image by ID",
        description = "Removes an image from the database using its ID.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Image successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Image not found", content = @Content)
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id) {
        this.imageService.removeImage(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Retrieves a list of all images.
     * 
     * @return a ResponseEntity with the list of images or an error response
     */
    @Operation(
        summary = "Retrieve all images",
        description = "Returns a list of all images stored in the database.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of images retrieved", content = @Content(schema = @Schema(implementation = ImageEntity.class)))
        }
    )
    @GetMapping
    public ResponseEntity<List<ImageEntity>> getAllImages() {
        List<ImageEntity> images = this.imageService.getAllImages();
        return ResponseEntity.ok(images);
    }
}
