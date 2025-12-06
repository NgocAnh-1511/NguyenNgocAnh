import {
  Entity,
  Column,
  PrimaryColumn,
} from 'typeorm';

@Entity('users')
export class User {
  @PrimaryColumn({ name: 'user_id' })
  userId: string;

  @Column({ name: 'phone_number' })
  phoneNumber: string;

  @Column({ name: 'full_name', nullable: true })
  fullName: string;

  @Column({ nullable: true })
  email: string;

  @Column({ nullable: true })
  password: string;

  @Column({ name: 'avatar_path', nullable: true })
  avatarPath: string;

  @Column({ name: 'created_at', type: 'bigint' })
  createdAt: number;

  @Column({ name: 'is_logged_in', type: 'tinyint', default: 0 })
  isLoggedIn: number;

  @Column({ name: 'auth_token', nullable: true })
  authToken: string;

  @Column({ name: 'is_admin', type: 'tinyint', default: 0 })
  isAdmin: number;
}

