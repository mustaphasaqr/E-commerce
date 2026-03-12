import { z } from 'zod'

export const productSchema = z.object({
  id: z.string(),
  name: z.string(),
  price: z.number().positive(),
  description: z.string().optional(),
  image: z.string().optional(),
  category: z.string(),
})

export type Product = z.infer<typeof productSchema>
