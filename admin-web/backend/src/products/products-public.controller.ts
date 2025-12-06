import {
    Controller,
    Get,
    Param,
    ParseIntPipe,
    Query,
} from '@nestjs/common';
import { ProductsService } from './products.service';

/**
 * Public Products Controller
 * Không cần authentication - dành cho Android app
 */
@Controller('public/products')
export class ProductsPublicController {
    constructor(private readonly productsService: ProductsService) { }

    /**
     * Lấy tất cả sản phẩm (public)
     * GET /api/public/products
     */
    @Get()
    findAll(@Query('categoryId') categoryId?: string) {
        if (categoryId) {
            return this.productsService.findByCategory(parseInt(categoryId));
        }
        return this.productsService.findAll();
    }

    /**
     * Lấy chi tiết sản phẩm (public)
     * GET /api/public/products/:id
     */
    @Get(':id')
    findOne(@Param('id', ParseIntPipe) id: number) {
        return this.productsService.findOne(id);
    }
}

