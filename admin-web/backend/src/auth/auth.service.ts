import { Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import * as bcrypt from 'bcrypt';
import { User } from '../users/entities/user.entity';
import { LoginDto } from './dto/login.dto';
import { RegisterDto } from './dto/register.dto';

@Injectable()
export class AuthService {
  constructor(
    @InjectRepository(User)
    private userRepository: Repository<User>,
    private jwtService: JwtService,
  ) {}

  async validateUser(phoneNumber: string, password: string): Promise<any> {
    const user = await this.userRepository.findOne({
      where: { phoneNumber },
    });

    if (!user) {
      return null;
    }

    // Kiểm tra password (có thể là plain text hoặc đã hash)
    let passwordMatch = false;
    if (user.password) {
      // Nếu password có dấu $ thì là bcrypt hash, ngược lại là plain text
      if (user.password.startsWith('$2')) {
        passwordMatch = await bcrypt.compare(password, user.password);
      } else {
        passwordMatch = user.password === password;
      }
    }

    if (passwordMatch) {
      const { password, ...result } = user;
      return result;
    }
    return null;
  }

  async login(loginDto: LoginDto) {
    const user = await this.validateUser(loginDto.phoneNumber, loginDto.password);
    if (!user) {
      throw new UnauthorizedException('Invalid credentials');
    }

    const payload = { 
      sub: user.userId, 
      phoneNumber: user.phoneNumber, 
      isAdmin: user.isAdmin === 1 
    };
    return {
      access_token: this.jwtService.sign(payload),
      user: {
        userId: user.userId,
        phoneNumber: user.phoneNumber,
        fullName: user.fullName,
        email: user.email,
        isAdmin: user.isAdmin === 1,
      },
    };
  }

  async register(registerDto: RegisterDto) {
    const existingUser = await this.userRepository.findOne({
      where: { phoneNumber: registerDto.phoneNumber },
    });

    if (existingUser) {
      throw new UnauthorizedException('Phone number already exists');
    }

    const hashedPassword = await bcrypt.hash(registerDto.password, 10);
    const userId = `user_${Date.now()}`;
    const user = this.userRepository.create({
      userId,
      phoneNumber: registerDto.phoneNumber,
      fullName: registerDto.fullName || registerDto.phoneNumber, // Default to phone number if empty
      email: registerDto.email || null,
      password: hashedPassword,
      avatarPath: null,
      createdAt: Date.now(),
      isLoggedIn: 0,
      authToken: null,
      isAdmin: 0,
    });

    const savedUser = await this.userRepository.save(user);
    const { password, ...result } = savedUser;

    const payload = { 
      sub: savedUser.userId, 
      phoneNumber: savedUser.phoneNumber, 
      isAdmin: savedUser.isAdmin === 1 
    };
    return {
      access_token: this.jwtService.sign(payload),
      user: {
        userId: result.userId,
        phoneNumber: result.phoneNumber,
        fullName: result.fullName,
        email: result.email,
        isAdmin: result.isAdmin === 1,
      },
    };
  }

  async getProfile(userId: string) {
    const user = await this.userRepository.findOne({ where: { userId } });
    if (!user) {
      throw new UnauthorizedException('User not found');
    }
    const { password, ...result } = user;
    return {
      userId: result.userId,
      phoneNumber: result.phoneNumber,
      fullName: result.fullName,
      email: result.email,
      isAdmin: result.isAdmin === 1,
    };
  }
}

