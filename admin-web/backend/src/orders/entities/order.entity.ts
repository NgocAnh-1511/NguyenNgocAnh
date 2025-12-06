import {
  Entity,
  Column,
  PrimaryColumn,
} from 'typeorm';

export enum OrderStatus {
  PENDING = 'Pending',
  CONFIRMED = 'Confirmed',
  PROCESSING = 'Processing',
  SHIPPING = 'Shipping',
  DELIVERED = 'Delivered',
  CANCELLED = 'Cancelled',
  COMPLETED = 'Completed',
}

@Entity('orders')
export class Order {
  @PrimaryColumn({ name: 'order_id' })
  orderId: string;

  @Column({ name: 'user_id' })
  userId: string;

  @Column({ name: 'total_price', type: 'decimal', precision: 10, scale: 2 })
  totalPrice: number;

  @Column({ name: 'order_date', type: 'bigint' })
  orderDate: number;

  @Column()
  status: string;

  @Column({ name: 'delivery_address', nullable: true })
  deliveryAddress: string;

  @Column({ name: 'phone_number', nullable: true })
  phoneNumber: string;

  @Column({ name: 'customer_name', nullable: true })
  customerName: string;

  @Column({ name: 'payment_method', nullable: true })
  paymentMethod: string;
}

