import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Order, OrderStatus } from './entities/order.entity';
import { OrderItem } from './entities/order-item.entity';
import { CreateOrderDto } from './dto/create-order.dto';
import { UpdateOrderDto } from './dto/update-order.dto';

@Injectable()
export class OrdersService {
  constructor(
    @InjectRepository(Order)
    private orderRepository: Repository<Order>,
    @InjectRepository(OrderItem)
    private orderItemRepository: Repository<OrderItem>,
  ) {}

  async create(createOrderDto: CreateOrderDto): Promise<Order> {
    // Generate order ID
    const orderId = `order_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const orderDate = Date.now();
    
    // Create order
    const order = this.orderRepository.create({
      orderId,
      userId: createOrderDto.userId,
      totalPrice: createOrderDto.totalPrice,
      orderDate,
      status: createOrderDto.status || 'Pending',
      deliveryAddress: createOrderDto.deliveryAddress || null,
      phoneNumber: createOrderDto.phoneNumber || null,
      customerName: createOrderDto.customerName || null,
      paymentMethod: createOrderDto.paymentMethod || null,
    });

    const savedOrder = await this.orderRepository.save(order);

    // Create order items
    if (createOrderDto.items && createOrderDto.items.length > 0) {
      const orderItems = createOrderDto.items.map(item => {
        const subtotal = item.price * item.quantity;
        return this.orderItemRepository.create({
          orderId: savedOrder.orderId,
          itemTitle: item.productName,
          itemJson: item.itemJson || JSON.stringify({ productName: item.productName, price: item.price }),
          quantity: item.quantity,
          price: item.price,
          subtotal,
        });
      });

      await this.orderItemRepository.save(orderItems);
    }

    return savedOrder;
  }

  async findAll(): Promise<Order[]> {
    return this.orderRepository.find({
      order: { orderDate: 'DESC' },
    });
  }

  private async findOrderEntity(orderId: string): Promise<Order> {
    const order = await this.orderRepository.findOne({
      where: { orderId },
    });
    if (!order) {
      throw new NotFoundException(`Order with ID ${orderId} not found`);
    }
    return order;
  }

  async findOne(orderId: string): Promise<any> {
    const order = await this.findOrderEntity(orderId);
    
    // Get order items
    const orderItems = await this.orderItemRepository.find({
      where: { orderId },
    });
    
    return {
      ...order,
      items: orderItems,
    };
  }

  async update(orderId: string, updateOrderDto: UpdateOrderDto): Promise<Order> {
    const order = await this.findOrderEntity(orderId);
    Object.assign(order, updateOrderDto);
    return this.orderRepository.save(order);
  }

  async updateStatus(orderId: string, status: string): Promise<Order> {
    const order = await this.findOrderEntity(orderId);
    order.status = status;
    return this.orderRepository.save(order);
  }

  async remove(orderId: string): Promise<void> {
    const order = await this.findOrderEntity(orderId);
    await this.orderRepository.remove(order);
  }
}

