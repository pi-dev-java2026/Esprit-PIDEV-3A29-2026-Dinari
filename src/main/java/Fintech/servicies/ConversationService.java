package Fintech.servicies;

import Fintech.entities.Conversation;
import Fintech.entities.Message;
import Fintech.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationService {
    private Connection cnx;

    public ConversationService() {
        cnx = MyDataBase.getInstance().getMyConnection();
    }

    public Conversation creerConversation(String userEmail) {
        String query = "INSERT INTO conversations (user_email, date_creation) VALUES (?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            pst.setString(1, userEmail);
            pst.setTimestamp(2, now);
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                return new Conversation(id, userEmail, now);
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la création de la conversation : " + ex.getMessage());
        }
        return null;
    }

    public void ajouterMessage(Message message) {
        String query = "INSERT INTO messages (conversation_id, sender, content, date_message) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            message.setDateMessage(now);

            pst.setInt(1, message.getConversationId());
            pst.setString(2, message.getSender());
            pst.setString(3, message.getContent());
            pst.setTimestamp(4, now);

            pst.executeUpdate();
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                message.setId(rs.getInt(1));
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de l'ajout du message : " + ex.getMessage());
        }
    }

    public List<Message> getHistorique(int conversationId, int limit) {
        List<Message> messages = new ArrayList<>();
        // Get the latest 'limit' messages, ordered by date ascending
        // We order by date descending to limit the most recent, then reverse the list
        // to have chronological order.
        String query = "SELECT * FROM messages WHERE conversation_id = ? ORDER BY date_message DESC LIMIT ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, conversationId);
            pst.setInt(2, limit);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Message m = new Message();
                m.setId(rs.getInt("id"));
                m.setConversationId(rs.getInt("conversation_id"));
                m.setSender(rs.getString("sender"));
                m.setContent(rs.getString("content"));
                m.setDateMessage(rs.getTimestamp("date_message"));
                messages.add(0, m); // Add at index 0 to reverse order (oldest first among the recent messages)
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la récupération de l'historique : " + ex.getMessage());
        }
        return messages;
    }
}
