package pl.szmaus.firebirdf00154attachment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "R3_DOCUMENT_FILES")

    public class R3DocumentFiles {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "GUID")
    private String guid;
    @Column(name="DATA")
    private byte[] data;

    @Override
    public String toString() {
        return "pdf [" + "GUID=" + guid + ", data=" + data +"]";
    }
}