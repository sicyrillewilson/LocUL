import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../viewmodels/infrastructure_vm.dart';
import '../../data/models/infrastructure.dart';
import 'dart:io';

class InfrastructuresPage extends StatefulWidget {
  const InfrastructuresPage({super.key});

  @override
  State<InfrastructuresPage> createState() => _InfrastructuresPageState();
}

class _InfrastructuresPageState extends State<InfrastructuresPage> {
  @override
  void initState() {
    super.initState();
    // déclenche le chargement une seule fois
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<InfrastructureVM>().load();
    });
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<InfrastructureVM>();

    return Scaffold(
      appBar: AppBar(title: const Text('Infrastructures')),
      body:
          vm.loading
              ? const Center(child: CircularProgressIndicator())
              : vm.items == null
              ? const Center(child: Text('Aucune infrastructure'))
              : ListView.builder(
                itemCount: vm.items!.length,
                itemBuilder: (_, i) => _InfrastructureTile(vm.items![i]),
              ),
    );
  }
}

class _InfrastructureTile extends StatelessWidget {
  final Infrastructure i;
  const _InfrastructureTile(this.i);

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading:
          i.image.isEmpty
              ? const Icon(Icons.apartment)
              : File(i.image).existsSync()
              ? Image.file(File(i.image), width: 56, fit: BoxFit.cover)
              : const Icon(Icons.broken_image),
      title: Text(i.nom),
      subtitle: Text(i.situation),
      onTap: () {
        // navigation vers une page détail si besoin
        print('Tap sur $i');
        print(
          'Chemin image: ${i.image}  Existe: ${File(i.image).existsSync()}',
        );
      },
    );
  }
}
