import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../viewmodels/map_vm.dart';
import '../../data/models/salle.dart';
import 'dart:io';
import 'package:cached_network_image/cached_network_image.dart';

class MapPage extends StatefulWidget {
  const MapPage({super.key});

  @override
  State<MapPage> createState() => _MapPageState();
}

class _MapPageState extends State<MapPage> {
  @override
  void initState() {
    super.initState();
    // déclenche le chargement une seule fois
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<MapVM>().load();
    });
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<MapVM>();

    return Scaffold(
      appBar: AppBar(title: const Text('Salles')),
      body:
          vm.loading
              ? const Center(child: CircularProgressIndicator())
              : vm.items == null
              ? const Center(child: Text('Aucune salle'))
              : ListView.builder(
                itemCount: vm.items!.length,
                itemBuilder: (_, i) => _SalleTile(vm.items![i]),
              ),
    );
  }
}

class _SalleTile extends StatelessWidget {
  final Salle s;
  const _SalleTile(this.s);

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading:
          /*s.image.isEmpty
              ? const Icon(Icons.apartment)
              : File(s.image).existsSync()
              ? Image.file(File(s.image), width: 56, fit: BoxFit.cover)
              : const Icon(Icons.broken_image),*/
          s.image.isEmpty
              ? const Icon(Icons.apartment)
              : CachedNetworkImage(
                imageUrl: s.image,
                width: 56,
                height: 56,
                fit: BoxFit.cover,
                placeholder:
                    (context, url) =>
                        const CircularProgressIndicator(), // En attendant le chargement
                errorWidget:
                    (context, url, error) =>
                        const Icon(Icons.broken_image), // Si erreur
              ),
      title: Text(s.nom),
      subtitle: Text(s.situation),
      onTap: () {
        // navigation vers une page détail si besoin
        print('Tap sur $s');
        print(
          'Chemin image: ${s.image}  Existe: ${File(s.image).existsSync()}',
        );
      },
    );
  }
}

/*class MapPage extends StatelessWidget {
  const MapPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Maps')),
      body: const Center(child: Text('Bienvenue !')),
    );
  }
}*/
