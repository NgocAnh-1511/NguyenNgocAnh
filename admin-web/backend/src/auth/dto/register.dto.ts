import { IsString, IsNotEmpty, IsEmail, IsOptional, ValidateIf } from 'class-validator';

export class RegisterDto {
  @IsString()
  @IsNotEmpty()
  phoneNumber: string;

  @IsString()
  @IsNotEmpty()
  password: string;

  @IsString()
  @IsOptional()
  fullName?: string;

  @ValidateIf((o) => o.email !== null && o.email !== undefined && o.email !== '')
  @IsEmail()
  @IsOptional()
  email?: string;
}

