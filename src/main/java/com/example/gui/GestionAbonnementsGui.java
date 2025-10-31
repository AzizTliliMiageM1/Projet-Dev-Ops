package com.example.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.example.abonnement.Abonnement;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projet.repository.AbonnementRepository;
import com.projet.repository.FileAbonnementRepository;

/**
 * Petite interface graphique Swing pour gérer les abonnements.
 * Ceci est volontairement simple et visuel — style étudiant.
 */
public class GestionAbonnementsGui extends JFrame {
    private final AbonnementRepository repo;
    private final DefaultListModel<Abonnement> listModel;
    private final JList<Abonnement> abonnementJList;
    private final ObjectMapper mapper;

    public GestionAbonnementsGui() {
        super("Gestion d'abonnements — Interface graphique");
        this.repo = new FileAbonnementRepository("abonnements.txt");
        this.listModel = new DefaultListModel<>();
        this.abonnementJList = new JList<>(listModel);

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        initUi();
        loadData();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
    }

    private void initUi() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        JLabel title = new JLabel("Gestion d'abonnements");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(new Color(33, 37, 41));
        root.add(title, BorderLayout.NORTH);

        // Center: list with custom renderer
        abonnementJList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JPanel p = new JPanel(new BorderLayout());
            p.setBorder(new EmptyBorder(6, 6, 6, 6));
            JLabel l = new JLabel(value.getClientName() + " — " + value.getNomService());
            l.setFont(new Font("SansSerif", Font.PLAIN, 14));
            JLabel sub = new JLabel("De " + value.getDateDebut() + " à " + value.getDateFin() + " — " + String.format("%.2f€", value.getPrixMensuel()));
            sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
            sub.setForeground(Color.DARK_GRAY);
            p.add(l, BorderLayout.NORTH);
            p.add(sub, BorderLayout.SOUTH);
            if (isSelected) {
                p.setBackground(new Color(220, 235, 255));
            } else {
                p.setBackground(Color.WHITE);
            }
            return p;
        });

        JScrollPane scroll = new JScrollPane(abonnementJList);
        root.add(scroll, BorderLayout.CENTER);

        // Right: buttons
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setBorder(new EmptyBorder(0, 10, 0, 0));

        JButton addBtn = createButton("Ajouter", new Color(40, 167, 69));
        addBtn.addActionListener(this::onAdd);
        JButton editBtn = createButton("Modifier", new Color(23, 162, 184));
        editBtn.addActionListener(this::onEdit);
        JButton delBtn = createButton("Supprimer", new Color(220, 53, 69));
        delBtn.addActionListener(this::onDelete);
        JButton exportBtn = createButton("Exporter JSON", new Color(255, 193, 7));
        exportBtn.addActionListener(this::onExport);
        JButton importBtn = createButton("Importer JSON", new Color(108, 117, 125));
        importBtn.addActionListener(this::onImport);
        JButton refreshBtn = createButton("Rafraîchir", new Color(0, 123, 255));
        refreshBtn.addActionListener(e -> loadData());

        buttons.add(addBtn);
        buttons.add(Box.createRigidArea(new Dimension(0, 6)));
        buttons.add(editBtn);
        buttons.add(Box.createRigidArea(new Dimension(0, 6)));
        buttons.add(delBtn);
        buttons.add(Box.createRigidArea(new Dimension(0, 6)));
        buttons.add(exportBtn);
        buttons.add(Box.createRigidArea(new Dimension(0, 6)));
        buttons.add(importBtn);
        buttons.add(Box.createRigidArea(new Dimension(0, 6)));
        buttons.add(refreshBtn);

        root.add(buttons, BorderLayout.EAST);

        add(root);
    }

    private JButton createButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setMaximumSize(new Dimension(200, 36));
        return b;
    }

    private void loadData() {
        listModel.clear();
        List<Abonnement> all = repo.findAll();
        for (Abonnement a : all) listModel.addElement(a);
    }

    private void onAdd(ActionEvent ev) {
        Abonnement a = showEditDialog(null);
        if (a != null) {
            repo.save(a);
            loadData();
        }
    }

    private void onEdit(ActionEvent ev) {
        Abonnement sel = abonnementJList.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Sélectionne un abonnement à modifier.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Abonnement updated = showEditDialog(sel);
        if (updated != null) {
            // replace in list and save all
            List<Abonnement> all = repo.findAll();
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).equals(sel)) {
                    all.set(i, updated);
                    break;
                }
            }
            repo.saveAll(all);
            loadData();
        }
    }

    private void onDelete(ActionEvent ev) {
        Abonnement sel = abonnementJList.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Sélectionne un abonnement à supprimer.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "Confirmer la suppression de " + sel.getClientName() + " ?", "Confirmer", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            repo.delete(sel);
            loadData();
        }
    }

    private void onExport(ActionEvent ev) {
        try {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("abonnements_export.json"));
            int ret = fc.showSaveDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                mapper.writerWithDefaultPrettyPrinter().writeValue(new FileWriter(f), repo.findAll());
                JOptionPane.showMessageDialog(this, "Exporté dans " + f.getAbsolutePath());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur export: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onImport(ActionEvent ev) {
        try {
            JFileChooser fc = new JFileChooser();
            int ret = fc.showOpenDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                Abonnement[] arr = mapper.readValue(new FileReader(f), Abonnement[].class);
                for (Abonnement a : arr) repo.save(a);
                loadData();
                JOptionPane.showMessageDialog(this, "Import réussi (" + arr.length + " abonnements). ");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur import: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Abonnement showEditDialog(Abonnement source) {
        JTextField clientField = new JTextField(source != null ? source.getClientName() : "");
        JTextField serviceField = new JTextField(source != null ? source.getNomService() : "");
        JTextField debutField = new JTextField(source != null && source.getDateDebut() != null ? source.getDateDebut().toString() : "");
        JTextField finField = new JTextField(source != null && source.getDateFin() != null ? source.getDateFin().toString() : "");
        JTextField prixField = new JTextField(source != null ? String.valueOf(source.getPrixMensuel()) : "0.0");
        JTextField categorieField = new JTextField(source != null ? source.getCategorie() : "");

        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(new JLabel("Nom du client:")); p.add(clientField);
        p.add(new JLabel("Nom du service:")); p.add(serviceField);
        p.add(new JLabel("Date début (YYYY-MM-DD):")); p.add(debutField);
        p.add(new JLabel("Date fin (YYYY-MM-DD):")); p.add(finField);
        p.add(new JLabel("Prix mensuel:")); p.add(prixField);
        p.add(new JLabel("Catégorie:")); p.add(categorieField);

        int res = JOptionPane.showConfirmDialog(this, p, source == null ? "Ajouter Abonnement" : "Modifier Abonnement", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return null;

        try {
            String client = clientField.getText().trim();
            String service = serviceField.getText().trim();
            LocalDate debut = debutField.getText().trim().isEmpty() ? LocalDate.now() : LocalDate.parse(debutField.getText().trim());
            LocalDate fin = finField.getText().trim().isEmpty() ? debut : LocalDate.parse(finField.getText().trim());
            double prix = Double.parseDouble(prixField.getText().trim());
            String cat = categorieField.getText().trim().isEmpty() ? "Non classé" : categorieField.getText().trim();
            if (source == null) {
                return new Abonnement(service, debut, fin, prix, client, debut, cat);
            } else {
                return new Abonnement(service, debut, fin, prix, client, source.getDerniereUtilisation() != null ? source.getDerniereUtilisation() : debut, cat);
            }
        } catch (DateTimeParseException dte) {
            JOptionPane.showMessageDialog(this, "Format de date invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return null;
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Prix invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GestionAbonnementsGui g = new GestionAbonnementsGui();
            g.setVisible(true);
        });
    }
}
