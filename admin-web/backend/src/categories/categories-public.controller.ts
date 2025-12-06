import {
    Controller,
    Get,
    Param,
    ParseIntPipe,
} from '@nestjs/common';
import { CategoriesService } from './categories.service';

/**
 * Public Categories Controller
 * Không cần authentication - dành cho Android app
 */
@Controller('public/categories')
export class CategoriesPublicController {
    constructor(private readonly categoriesService: CategoriesService) { }

    /**
     * Lấy tất cả danh mục (public)
     * GET /api/public/categories
     */
    @Get()
    findAll() {
        return this.categoriesService.findAll();
    }

    /**
     * Lấy chi tiết danh mục (public)
     * GET /api/public/categories/:id
     */
    @Get(':id')
    findOne(@Param('id', ParseIntPipe) id: number) {
        return this.categoriesService.findOne(id);
    }
}

