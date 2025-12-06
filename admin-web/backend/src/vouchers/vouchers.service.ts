import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Voucher } from './entities/voucher.entity';
import { CreateVoucherDto } from './dto/create-voucher.dto';
import { UpdateVoucherDto } from './dto/update-voucher.dto';

@Injectable()
export class VouchersService {
  constructor(
    @InjectRepository(Voucher)
    private voucherRepository: Repository<Voucher>,
  ) {}

  async create(createVoucherDto: CreateVoucherDto): Promise<Voucher> {
    // Generate voucher ID
    const voucherId = `voucher_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    // Convert Date to timestamp (number)
    const startDate = createVoucherDto.startDate 
      ? (typeof createVoucherDto.startDate === 'string' 
          ? new Date(createVoucherDto.startDate).getTime() 
          : createVoucherDto.startDate.getTime())
      : Date.now();
    
    const endDate = createVoucherDto.endDate
      ? (typeof createVoucherDto.endDate === 'string'
          ? new Date(createVoucherDto.endDate).getTime()
          : createVoucherDto.endDate.getTime())
      : Date.now() + (30 * 24 * 60 * 60 * 1000); // Default: 30 days from now
    
    // Map DTO to Entity format
    const voucher = this.voucherRepository.create({
      voucherId,
      code: createVoucherDto.code,
      discountPercent: createVoucherDto.type === 'AMOUNT' ? 0 : createVoucherDto.value,
      discountAmount: createVoucherDto.type === 'AMOUNT' ? createVoucherDto.value : 0,
      discountType: createVoucherDto.type || 'PERCENT',
      minOrderAmount: createVoucherDto.minPurchaseAmount || null,
      maxDiscountAmount: createVoucherDto.maxDiscountAmount || null,
      startDate,
      endDate,
      usageLimit: createVoucherDto.usageLimit || 0,
      usedCount: 0,
      isActive: createVoucherDto.isActive !== false ? 1 : 0,
      description: createVoucherDto.description || null,
    });
    
    return this.voucherRepository.save(voucher);
  }

  async findAll(): Promise<Voucher[]> {
    try {
      const vouchers = await this.voucherRepository.find({
        order: { voucherId: 'DESC' },
      });
      console.log(`Found ${vouchers.length} vouchers`);
      return vouchers;
    } catch (error) {
      console.error('Error fetching vouchers:', error);
      console.error('Error details:', JSON.stringify(error, null, 2));
      // Return empty array instead of throwing to prevent 500 error
      // This allows the frontend to still work even if there's a DB issue
      return [];
    }
  }

  async findOne(voucherId: string): Promise<Voucher> {
    const voucher = await this.voucherRepository.findOne({
      where: { voucherId },
    });
    if (!voucher) {
      throw new NotFoundException(`Voucher with ID ${voucherId} not found`);
    }
    return voucher;
  }

  async findByCode(code: string): Promise<Voucher> {
    const voucher = await this.voucherRepository.findOne({
      where: { code },
    });
    if (!voucher) {
      throw new NotFoundException(`Voucher with code ${code} not found`);
    }
    return voucher;
  }

  async update(voucherId: string, updateVoucherDto: UpdateVoucherDto): Promise<Voucher> {
    const voucher = await this.findOne(voucherId);
    
    // Convert Date to timestamp if provided
    const updateData: any = { ...updateVoucherDto };
    
    if (updateVoucherDto.startDate !== undefined) {
      updateData.startDate = typeof updateVoucherDto.startDate === 'string'
        ? new Date(updateVoucherDto.startDate).getTime()
        : updateVoucherDto.startDate.getTime();
    }
    
    if (updateVoucherDto.endDate !== undefined) {
      updateData.endDate = typeof updateVoucherDto.endDate === 'string'
        ? new Date(updateVoucherDto.endDate).getTime()
        : updateVoucherDto.endDate.getTime();
    }
    
    // Map DTO fields to Entity fields
    if (updateVoucherDto.value !== undefined || updateVoucherDto.type !== undefined) {
      const type = updateVoucherDto.type || voucher.discountType;
      const value = updateVoucherDto.value !== undefined 
        ? updateVoucherDto.value 
        : (type === 'AMOUNT' ? voucher.discountAmount : voucher.discountPercent);
      
      updateData.discountPercent = type === 'AMOUNT' ? 0 : value;
      updateData.discountAmount = type === 'AMOUNT' ? value : 0;
      updateData.discountType = type;
      
      delete updateData.value;
      delete updateData.type;
    }
    
    if (updateVoucherDto.minPurchaseAmount !== undefined) {
      updateData.minOrderAmount = updateVoucherDto.minPurchaseAmount;
      delete updateData.minPurchaseAmount;
    }
    
    if (updateVoucherDto.isActive !== undefined) {
      updateData.isActive = updateVoucherDto.isActive ? 1 : 0;
    }
    
    Object.assign(voucher, updateData);
    return this.voucherRepository.save(voucher);
  }

  async remove(voucherId: string): Promise<void> {
    const voucher = await this.findOne(voucherId);
    await this.voucherRepository.remove(voucher);
  }
}

