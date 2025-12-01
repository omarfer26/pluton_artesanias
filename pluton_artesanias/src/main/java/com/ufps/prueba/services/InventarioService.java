package com.ufps.prueba.services;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufps.prueba.entities.Inventario;
import com.ufps.prueba.entities.InventarioMaterial;
import com.ufps.prueba.entities.Producto;
import com.ufps.prueba.entities.ProductoMaterial;
import com.ufps.prueba.repositories.InventarioMaterialRepository;
import com.ufps.prueba.repositories.InventarioRepository;
import com.ufps.prueba.repositories.ProductoMaterialRepository;

import jakarta.transaction.Transactional;

@Service
public class InventarioService {

    @Autowired
    private InventarioRepository inventarioRepo;

    @Autowired
    private InventarioMaterialRepository inventarioMaterialRepo;

    @Autowired
    private ProductoMaterialRepository productoMaterialRepo;

    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private AlertaInventarioService alertaService;

    // --- Método nuevo para productos ---
    public int obtenerStockProducto(Long productoId) {
        Inventario inv = obtenerInventarioProducto(productoId);
        return inv.getCantidad() - inv.getReservado(); // stock disponible
    }

    // --- Método nuevo para materiales ---
    public int obtenerStockMaterial(Long materialId) {
        InventarioMaterial inv = obtenerInventarioMaterial(materialId);
        return inv.getCantidad(); // no hay reservado para materiales, solo cantidad
    }

    // --- Método para avisar si el stock está en mínimo ---
    public boolean estaEnMinimoProducto(Long productoId) {
        Inventario inv = obtenerInventarioProducto(productoId);
        return inv.getCantidad() >= 0;
    }

    public boolean estaEnMinimoMaterial(Long materialId) {
        InventarioMaterial inv = obtenerInventarioMaterial(materialId);
        return inv.getCantidad() >= 0; // suponiendo que InventarioMaterial tiene 'minimo'
    }
    
    public Inventario obtenerInventarioProducto(Long productoId) {
        return inventarioRepo.findByProductoId(productoId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado para producto " + productoId));
    }

    public void agregarStockProducto(Long productoId, int cantidad) {
        Inventario inv;
        try {
            // Intentar obtener el inventario existente
            inv = obtenerInventarioProducto(productoId);
            // Sumar la cantidad
            inv.setCantidad(inv.getCantidad() + cantidad);
        } catch (RuntimeException e) {
            // Si no existe, crear uno nuevo con cantidad inicial igual a 'cantidad'
            Producto producto = productoService.obtenerProductoPorId(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado para agregar stock"));
            inv = Inventario.builder()
                    .producto(producto)
                    .cantidad(cantidad)
                    .reservado(0)
                    .actualizadoEn(LocalDateTime.now())
                    .build();
        }

        inv.setActualizadoEn(LocalDateTime.now());
        inventarioRepo.save(inv);

        alertaService.verificarProducto(inv);
    }

    @Transactional
    public void reservarProducto(Long productoId, int cantidad) {
    	Inventario inventario = obtenerInventarioProducto(productoId);

        if (inventario.getCantidad() < cantidad) {
            throw new RuntimeException("Stock insuficiente para reservar");
        }

        inventario.setCantidad(inventario.getCantidad() - cantidad);
        inventario.setReservado(inventario.getReservado() + cantidad);

        inventarioRepo.save(inventario);
    }

    public void liberarReservaProducto(Long productoId, int cantidad) {
        Inventario inv = obtenerInventarioProducto(productoId);
        inv.setReservado(inv.getReservado() - cantidad);
        inventarioRepo.save(inv);
    }

    public void consumirProducto(Long productoId, int cantidad) {
        Inventario inv = obtenerInventarioProducto(productoId);

        if (inv.getCantidad() < cantidad) {
            throw new RuntimeException("No hay suficientes productos para consumir");
        }

        inv.setCantidad(inv.getCantidad() - cantidad);
        inv.setReservado(inv.getReservado() - cantidad);
        inventarioRepo.save(inv);

        alertaService.verificarProducto(inv);
    }

    
    public InventarioMaterial obtenerInventarioMaterial(Long materialId) {
        return inventarioMaterialRepo.findByMaterialId(materialId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado para material " + materialId));
    }

    public void agregarStockMaterial(Long materialId, int cantidad) {
        InventarioMaterial inv = obtenerInventarioMaterial(materialId);
        inv.setCantidad(inv.getCantidad() + cantidad);
        inv.setActualizadoEn(LocalDateTime.now());
        inventarioMaterialRepo.save(inv);

        alertaService.verificarMaterial(inv);
    }

    public void consumirMaterial(Long materialId, int cantidad) {
        InventarioMaterial inv = obtenerInventarioMaterial(materialId);

        if (inv.getCantidad() < cantidad) {
            throw new RuntimeException("No hay suficiente material en bodega");
        }

        inv.setCantidad(inv.getCantidad() - cantidad);
        inventarioMaterialRepo.save(inv);

        alertaService.verificarMaterial(inv);
    }
    
    public void consumirMaterialesPorProducto(Long productoId, int cantidad) {

        List<ProductoMaterial> lista = productoMaterialRepo.findByProductoId(productoId);

        for (ProductoMaterial pm : lista) {
            int totalConsumir = pm.getCantidadUsada() * cantidad;
            consumirMaterial(pm.getMaterial().getId(), totalConsumir);
        }
    }
    
    public boolean puedeFabricar(Long productoId, int cantidad) {

        List<ProductoMaterial> lista = productoMaterialRepo.findByProductoId(productoId);

        for (ProductoMaterial pm : lista) {
            int necesario = pm.getCantidadUsada() * cantidad;

            InventarioMaterial inv = obtenerInventarioMaterial(pm.getMaterial().getId());

            if (inv.getCantidad() < necesario) {
                return false; 
            }
        }
        return true;
    }
    
    @Transactional
    public Inventario crearInventario(Long productoId, int cantidadInicial) {
        // Verificar si ya existe inventario
        Optional<Inventario> existente = inventarioRepo.findByProductoId(productoId);
        if (existente.isPresent()) {
            return existente.get(); // Si ya existe, simplemente lo retornamos
        }

        // Obtener el producto
        Producto producto = productoService.obtenerProductoPorId(productoId)
                                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Crear inventario inicial
        Inventario inv = Inventario.builder()
                .producto(producto)
                .cantidad(cantidadInicial)
                .reservado(0)
                .actualizadoEn(LocalDateTime.now())
                .build();

        return inventarioRepo.save(inv);
    }

    
    public List<Inventario> listarInventarioProductos() {
        return inventarioRepo.findAll();
    }
    
    public List<InventarioMaterial> listarInventarioMaterial() {
        return inventarioMaterialRepo.findAll();
    }
    
}
