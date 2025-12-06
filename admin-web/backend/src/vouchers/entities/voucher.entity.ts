import {
  Entity,
  Column,
  PrimaryColumn,
} from 'typeorm';

export enum VoucherType {
  PERCENT = 'PERCENT',
  AMOUNT = 'AMOUNT',
}

@Entity('vouchers')
export class Voucher {
  @PrimaryColumn({ name: 'voucher_id' })
  voucherId: string;

  @Column({ name: 'code', unique: true })
  code: string;

  @Column({ name: 'discount_percent', type: 'decimal', precision: 5, scale: 2, default: 0 })
  discountPercent: number;

  @Column({ name: 'discount_amount', type: 'decimal', precision: 10, scale: 2, default: 0 })
  discountAmount: number;

  @Column({ name: 'discount_type', default: 'PERCENT' })
  discountType: string;

  @Column({ name: 'min_order_amount', type: 'decimal', precision: 10, scale: 2, nullable: true })
  minOrderAmount: number;

  @Column({ name: 'max_discount_amount', type: 'decimal', precision: 10, scale: 2, nullable: true })
  maxDiscountAmount: number;

  @Column({ name: 'start_date', type: 'bigint' })
  startDate: number;

  @Column({ name: 'end_date', type: 'bigint' })
  endDate: number;

  @Column({ name: 'usage_limit', type: 'int', default: 0 })
  usageLimit: number;

  @Column({ name: 'used_count', type: 'int', default: 0 })
  usedCount: number;

  @Column({ name: 'is_active', type: 'tinyint', default: 1 })
  isActive: number;

  @Column({ name: 'description', nullable: true })
  description: string;
}

