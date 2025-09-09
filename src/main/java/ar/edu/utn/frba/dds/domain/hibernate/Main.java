// src/main/java/ar/edu/utn/frba/dds/domain/hibernate/Main.java
      package ar.edu.utn.frba.dds.domain.hibernate;

      import org.hibernate.Session;
      import org.hibernate.SessionFactory;
      import org.hibernate.Transaction;
      import ar.edu.utn.frba.dds.domain.hibernate.Producto;
      

      public class Main {
        public static void main(String[] args) {

          // 1. Obtener el SessionFactory
          SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

          Session session = null;
          try {
            session = sessionFactory.openSession();

            // =======================================================
            // PRUEBA DE GUARDADO (CREATE) 💾
            // =======================================================
            System.out.println("--- Iniciando prueba de guardado... ---");
            Transaction transaction = session.beginTransaction();

            Producto nuevoProducto = new Producto();
            nuevoProducto.setNombre("Teclado Mecánico RGB");
            nuevoProducto.setPrecio(99.99);

            session.persist(nuevoProducto); // Guardamos el objeto
            transaction.commit(); // Confirmamos la transacción

            System.out.println("✅ Producto guardado con ID: " + nuevoProducto.getId());

            // =======================================================
            // PRUEBA DE LECTURA (READ) 🔍
            // =======================================================
            System.out.println("\n--- Iniciando prueba de lectura... ---");
            transaction = session.beginTransaction();

            // Buscamos el producto que acabamos de guardar por su ID
            Producto productoGuardado = session.get(Producto.class, nuevoProducto.getId());


            if (productoGuardado != null) {
              System.out.println("✅ Producto recuperado: " + productoGuardado.getNombre() + " - Precio: $" + productoGuardado.getPrecio());
            } else {
              System.out.println("❌ Error: No se pudo recuperar el producto.");
            }
            transaction.commit();

          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            if (session != null) {
              session.close();
            }
            sessionFactory.close();
          }
        }
      }